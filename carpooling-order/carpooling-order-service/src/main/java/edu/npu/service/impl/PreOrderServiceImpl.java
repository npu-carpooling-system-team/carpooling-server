package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.OrderStatusEnum;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.entity.*;
import edu.npu.feignClient.CarpoolingServiceClient;
import edu.npu.feignClient.DriverServiceClient;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.OrderMapper;
import edu.npu.service.PreOrderService;
import edu.npu.vo.DriverOrderListItem;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private UserServiceClient userServiceClient;

    @Resource
    private DriverServiceClient driverServiceClient;

    @Resource
    private CarpoolingServiceClient carpoolingServiceClient;

    @Override
    public R passengerApply(Long carpoolingId, LoginAccount loginAccount) {
        User currentUser = userServiceClient
                .getUserWithAccountUsername(loginAccount.getUsername());
        Order order = Order
                .builder()
                .carpoolingId(carpoolingId)
                .passengerId(currentUser.getId())
                .status(OrderStatusEnum.PRE_ORDER_REQUEST_SUBMITTED.getValue())
                .createTime(new Date())
                .build();
        boolean saveOrder = save(order);
        if (!saveOrder) {
            log.error("乘客申请拼车失败，乘客id：{}，拼车id：{}",
                    currentUser.getId(), carpoolingId);
            return R.error("乘客申请拼车失败,MySQL数据库异常");
        }
        return R.ok("乘客申请拼车成功,进入待审核阶段");
    }

    @Override
    public R driverGetConfirmList(Long carpoolingId, LoginAccount loginAccount) {
        User currentUser = userServiceClient
                .getUserWithAccountUsername(loginAccount.getUsername());
        if (!currentUser.getIsDriver()) {
            return R.error(ResponseCodeEnum.Forbidden, "该接口不允许非司机用户请求。");
        }
        // 需要校验行程ID是否是该司机的
        Driver driver = driverServiceClient.getDriverWithAccountUsername(
                loginAccount.getUsername()
        );
        Carpooling carpooling = carpoolingServiceClient.getCarpoolingById(carpoolingId);
        if (!carpooling.getDriverId().equals(driver.getId())) {
            return R.error(ResponseCodeEnum.Forbidden, "不允许访问不属于您的信息");
        }

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
}




