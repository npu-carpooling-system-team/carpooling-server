package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.OrderStatusEnum;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.entity.Carpooling;
import edu.npu.entity.Driver;
import edu.npu.entity.Order;
import edu.npu.feignClient.CarpoolingServiceClient;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.OrderMapper;
import edu.npu.service.FinishedOrderService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@Service
@Slf4j
public class FinishedOrderServiceImpl extends ServiceImpl<OrderMapper, Order>
            implements FinishedOrderService {

    @Resource
    private CarpoolingServiceClient carpoolingServiceClient;

    @Resource
    private UserServiceClient userServiceClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R rateDriver(Long orderId, Integer score) {
        if (orderId == null) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "参数错误,订单ID不允许为空");
        } else if (score == null) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "参数错误,评分不允许为空");
        }
        Order order = getById(orderId);
        if (!order.getStatus().equals(
                OrderStatusEnum.ORDER_NORMAL_CLOSED.getValue()
        )) {
            log.error("订单状态不正确，订单id:{}", orderId);
            return R.error("订单状态不正确");
        }

        // 根据当前订单查询行程 -> 根据行程查询司机
        // -> 查询司机所有行程ID -> 根据行程ID查询所有订单 -> 计算平均分 -> 更新司机分数
        Carpooling carpooling = carpoolingServiceClient.getCarpoolingById(
                order.getCarpoolingId()
        );
        List<Carpooling> carpoolingList =
                carpoolingServiceClient.getCarpoolingListByDriverId(carpooling.getDriverId());
        // 此时已完成付款 订单状态已经被置为NORMAL_CLOSED
        List<Order> orderList =
            list(
                new LambdaQueryWrapper<Order>()
                    .in(Order::getCarpoolingId, carpoolingList)
                    .eq(Order::getStatus,
                        OrderStatusEnum.ORDER_NORMAL_CLOSED.getValue())
            );
        // 统计总分 0分即为未评分 需要被排除
        AtomicInteger ignore = new AtomicInteger();
        // lambda表达式中的变量必须是final的或是原子的
        AtomicInteger totalRatePassenger = new AtomicInteger();
        AtomicInteger totalScore = new AtomicInteger();
        orderList.forEach(orderItem -> {
            if (orderItem.getScore() == 0) {
                ignore.getAndIncrement();
            } else {
                totalRatePassenger.getAndIncrement();
                totalScore.addAndGet(orderItem.getScore());
            }
        });
        Long newScore =
                (long) ((totalScore.get() + score) / (totalRatePassenger.get() + 1.0));
        Driver driver = userServiceClient.getDriverByAccountUsername(
                userServiceClient.getUserById(carpooling.getDriverId()).getUsername()
        );
        driver.setAvgScore(newScore);
        // 更新order表
        order.setScore(score);
        return userServiceClient.updateDriver(driver) && updateById(order) ? R.ok() :
                R.error(ResponseCodeEnum.SERVER_ERROR,"更新司机评分失败");
    }
}
