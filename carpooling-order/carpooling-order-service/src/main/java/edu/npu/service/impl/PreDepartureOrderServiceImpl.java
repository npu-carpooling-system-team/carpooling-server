package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.OrderStatusEnum;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.Order;
import edu.npu.entity.UnfinishedOrder;
import edu.npu.entity.User;
import edu.npu.exception.CarpoolingError;
import edu.npu.exception.CarpoolingException;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.OrderMapper;
import edu.npu.mapper.UnfinishedOrderMapper;
import edu.npu.service.PreDepartureOrderService;
import edu.npu.vo.R;
import groovy.util.logging.Slf4j;
import jakarta.annotation.Resource;
import org.apache.ibatis.javassist.Loader;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : [wangminan]
 * @description : [预定前业务实现类]
 */
@Service
@Slf4j
public class PreDepartureOrderServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements PreDepartureOrderService {

    @Resource
    OrderMapper orderMapper;

    @Resource
    UnfinishedOrderMapper unfinishedOrderMapper;

    @Resource
    private UserServiceClient userServiceClient;

    @Override
    public R userCancelOrder(Long orderId, LoginAccount loginAccount) {
        Order order = getById(orderId);
        boolean updateSuccess;
        if (order == null) {
            log.error("按orderID查询订单失败，订单不存在，订单id：{" + orderId + "}");
            return R.error("订单不存在");
        }
        User user = userServiceClient.getUserByAccountUsername(loginAccount.getUsername());
        if (user == null) {
            log.error("按username查询用户失败，用户不存在，username：{" + loginAccount.getUsername() + "}");
            return R.error("用户不存在");
        }
        Long userCancelTimes = this.searchUserCancelTimes(user);

        if (userCancelTimes >= 3) {

            order.setStatus(OrderStatusEnum.ARRIVED_USER_UNPAID.getValue());
            updateSuccess = updateById(order);

            if (updateSuccess) {

                UnfinishedOrder unfinishedOrder = new UnfinishedOrder();
                unfinishedOrder.setOrderId(orderId);
                unfinishedOrder.setCarpoolingId(order.getCarpoolingId());
                unfinishedOrder.setPassengerId(order.getPassengerId());

                int insertSuccess = unfinishedOrderMapper.insert(unfinishedOrder);
                if (1 == insertSuccess) {
                    return R.ok("用户半年内取消订单超过3次，强制支付");
                } else {
                    log.error("用户取消订单失败，订单id：{" + orderId + "}");
                    return R.error("用户取消订单失败，MySQL数据库异常");
                }
            } else {
                log.error("用户取消订单失败，订单id：{" + orderId + "}");
                return R.error("用户取消订单失败，MySQL数据库异常");
            }
        }
        order.setStatus(OrderStatusEnum.PRE_DEPARTURE_USER_CANCELLED.getValue());
        updateSuccess = updateById(order);

        if (updateSuccess) {
            return R.ok("用户取消订单成功");
        } else {
            log.error("用户取消订单失败，订单id：{" + orderId + "}");
            return R.error("用户取消订单失败，MySQL数据库异常");
        }

    }

    @Override
    public R searchUserCancelTimes(LoginAccount loginAccount) {
        User user = userServiceClient.getUserByAccountUsername(loginAccount.getUsername());
        if (user == null) {
            log.error("按username查询用户失败，用户不存在，username：{" + loginAccount.getUsername() + "}");
            return R.error("用户不存在");
        }
        Long userCancelTimes = this.searchUserCancelTimes(user);
        Map<String, Object> result = new HashMap<>();
        result.put("userCancelTimes", userCancelTimes);
        return R.ok(result);
    }

    public Long searchUserCancelTimes(User user) {
        Long userCancelTimes = 0L;
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -6);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date halfYearAgo = sdf.parse(sdf.format(cal.getTime()));
            userCancelTimes = orderMapper.selectCount(
                    new LambdaQueryWrapper<>(Order.class)
                            .eq(Order::getPassengerId, user.getId())
                            .eq(Order::getStatus, OrderStatusEnum.PRE_DEPARTURE_USER_CANCELLED.getValue())
                            .gt(Order::getUpdateTime, halfYearAgo)
            );


        } catch (ParseException e) {
            log.error("查询用户取消订单次数失败，时间转换异常");
            CarpoolingException.cast(e.getMessage());
        }
        return userCancelTimes;
    }

}
