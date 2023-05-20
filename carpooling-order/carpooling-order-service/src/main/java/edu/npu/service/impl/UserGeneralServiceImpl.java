package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.OrderStatusEnum;
import edu.npu.entity.Carpooling;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.Order;
import edu.npu.entity.User;
import edu.npu.feignClient.CarpoolingServiceClient;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.OrderMapper;
import edu.npu.service.UserGeneralService;
import edu.npu.vo.OrderDetailVo;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author : [wangminan]
 * @description : [用户通用服务实现类]
 */
@Service
public class UserGeneralServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements UserGeneralService {

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private CarpoolingServiceClient carpoolingServiceClient;

    @Override
    public R getOrderList(LoginAccount loginAccount) {
        User user = userServiceClient
                .getUserByAccountUsername(loginAccount.getUsername());
        List<Order> orders = list(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getPassengerId, user.getId())
                .orderByDesc(Order::getUpdateTime)
        );
        List<OrderDetailVo> orderDetailVos = new ArrayList<>();
        for (Order order : orders){
            Carpooling carpooling = carpoolingServiceClient
                .getCarpoolingById(order.getCarpoolingId());
            OrderDetailVo orderDetailVo = formOrder(carpooling, order);
            orderDetailVos.add(orderDetailVo);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", orderDetailVos);
        return R.ok(result);
    }

    @Override
    public R getOrderById(Long orderId) {
        Order order = getById(orderId);
        Carpooling carpooling = carpoolingServiceClient
                .getCarpoolingById(order.getCarpoolingId());
        OrderDetailVo orderDetailVo = formOrder(carpooling, order);
        Map<String, Object> result = new HashMap<>();
        result.put("result", orderDetailVo);
        return R.ok(result);
    }

    private OrderDetailVo formOrder(Carpooling carpooling, Order order){
        return OrderDetailVo.builder()
            .departurePoint(carpooling.getDeparturePoint())
            .arrivePoint(carpooling.getArrivePoint())
            .departureTime(carpooling.getDepartureTime())
            .arriveTime(carpooling.getArriveTime())
            .status(Objects.requireNonNull(
                    OrderStatusEnum.fromValue(order.getStatus())).name())
            .id(order.getId())
            .carpoolingId(order.getCarpoolingId())
            .score(order.getScore())
            .passengerId(order.getPassengerId())
            .createTime(order.getCreateTime())
            .updateTime(order.getUpdateTime())
            .passingPoint(carpooling.getPassingPoint())
            .build();
    }
}
