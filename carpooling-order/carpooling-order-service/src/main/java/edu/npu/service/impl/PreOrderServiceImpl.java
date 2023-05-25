package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.OrderStatusEnum;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.PassOrderDto;
import edu.npu.entity.*;
import edu.npu.feignClient.CarpoolingServiceClient;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.OrderMapper;
import edu.npu.service.PreOrderService;
import edu.npu.util.SendMailUtil;
import edu.npu.vo.DriverOrderListItem;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wangminan
 * @description 针对表【order(拼车订单表)】的数据库操作Service实现
 * @createDate 2023-04-20 14:21:40
 */
@Service
@Slf4j
public class PreOrderServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements PreOrderService {

    @Resource
    private SendMailUtil sendMailUtil;

    // 线程池
    private static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private CarpoolingServiceClient carpoolingServiceClient;

    @Resource
    private OrderMapper orderMapper;

    @Override
    public R passengerApply(Long carpoolingId, LoginAccount loginAccount) {
        User currentUser = userServiceClient
                .getUserByAccountUsername(loginAccount.getUsername());
        Carpooling carpooling =
                carpoolingServiceClient.getCarpoolingById(carpoolingId);
        if (carpooling.getDriverId().equals(currentUser.getId())) {
            return R.error(ResponseCodeEnum.CREATION_ERROR, "不允许向自己的行程下订单");
        }
        // mybatisplus的save方法在抽风 我自己写一个
        boolean saveOrder =
                orderMapper.save(carpoolingId, currentUser.getId(),
                        OrderStatusEnum.PRE_ORDER_REQUEST_SUBMITTED.getValue());
        if (!saveOrder) {
            log.error("乘客申请拼车失败，乘客id:{}，拼车id:{}",
                    currentUser.getId(), carpoolingId);
            return R.error("乘客申请拼车失败,MySQL数据库异常");
        }
        return R.ok("乘客申请拼车成功,进入待审核阶段");
    }

    @Override
    public R driverGetConfirmList(Long carpoolingId, LoginAccount loginAccount) {
        R forbidden = confirmForbidden(carpoolingId, loginAccount);
        if (forbidden != null) return forbidden;

        // 提取请求
        List<Order> orders = this.list(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getCarpoolingId, carpoolingId)
                        .eq(Order::getStatus,
                                OrderStatusEnum.PRE_ORDER_REQUEST_SUBMITTED.getValue())
        );
        List<DriverOrderListItem> list = new ArrayList<>();
        for (Order order : orders) {
            User passenger = userServiceClient.getUserById(order.getPassengerId());
            DriverOrderListItem driverOrderListItem = DriverOrderListItem
                    .builder()
                    .orderId(order.getId())
                    .passengerId(passenger.getId())
                    .passengerName(passenger.getUsername())
                    .applyTime(order.getCreateTime())
                    .build();
            list.add(driverOrderListItem);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        return R.ok(result);
    }

    @Override
    public R driverConfirm(PassOrderDto passOrderDto, LoginAccount loginAccount) {
        // 更新表信息
        Order order = getById(passOrderDto.orderId());
        if (passOrderDto.pass()) {
            // 更新表
            order.setStatus(OrderStatusEnum.PRE_ORDER_REQUEST_PASSED.getValue());
        } else {
            // 否则更新为强制结束
            order.setStatus(OrderStatusEnum.ORDER_FORCE_CLOSED.getValue());
        }
        // 另起一个线程发送提醒邮件
        cachedThreadPool.execute(() -> {
            String email = userServiceClient
                    .getUserById(order.getPassengerId()).getEmail();
            if (StringUtils.hasText(email)) {
                boolean sendMail = sendMailUtil.sendMail(
                        email,
                        "您的订单:" + passOrderDto.orderId() + "拼车申请结果",
                        passOrderDto.pass() ? "您的拼车申请已通过" : "您的拼车申请未通过"
                );
                if (!sendMail){
                    log.error("发送邮件失败，乘客id:{}，订单id:{}",
                            order.getPassengerId(), order.getId());
                }
            }
        });
        boolean save = updateById(order);
        if (!save) {
            log.error("司机确认乘客拼车申请失败，订单id:{}，乘客id:{}",
                    passOrderDto.orderId(), order.getPassengerId());
            return R.error("司机确认乘客拼车申请失败,MySQL数据库异常");
        }
        return R.ok("司机确认乘客拼车申请成功");
    }

    /**
     * 司机接口预校验
     *
     * @param carpoolingId 拼车ID
     * @param loginAccount 登录用户信息
     * @return 校验未通过返回R.error 否则返回null
     */
    private R confirmForbidden(Long carpoolingId, LoginAccount loginAccount) {
        User currentUser = userServiceClient
                .getUserByAccountUsername(loginAccount.getUsername());
        if (!currentUser.getIsDriver()) {
            return R.error(ResponseCodeEnum.FORBIDDEN, "该接口不允许非司机用户请求。");
        }
        // 需要校验行程ID是否是该司机的
        Driver driver = userServiceClient.getDriverByAccountUsername(
                loginAccount.getUsername()
        );
        Carpooling carpooling = carpoolingServiceClient.getCarpoolingById(carpoolingId);
        if (!carpooling.getDriverId().equals(driver.getDriverId())) {
            return R.error(ResponseCodeEnum.FORBIDDEN, "不允许访问不属于您的信息");
        }
        return null;
    }
}




