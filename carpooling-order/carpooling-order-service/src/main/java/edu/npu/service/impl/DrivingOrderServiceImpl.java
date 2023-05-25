package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.OrderStatusEnum;
import edu.npu.entity.*;
import edu.npu.feignClient.CarpoolingServiceClient;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.OrderMapper;
import edu.npu.mapper.UnfinishedOrderMapper;
import edu.npu.service.DrivingOrderService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author : [wangminan]
 * @description : [行车阶段业务实现类]
 */
@Service
@Slf4j
public class DrivingOrderServiceImpl extends ServiceImpl<OrderMapper, Order>
    implements DrivingOrderService {

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private CarpoolingServiceClient carpoolingServiceClient;

    @Resource
    private UnfinishedOrderMapper unfinishedOrderMapper;

    @Override
    public R passengerConfirmDeparture(Long orderId, LoginAccount loginAccount) {
        Order order = getById(orderId);

        if (order == null) {
            log.error("按orderID查询订单失败，orderAndUserExists，订单id:{}", orderId);
            return R.error("orderAndUserExists");
        } else if (!order.getStatus().equals(
                OrderStatusEnum.PRE_ORDER_REQUEST_PASSED.getValue()
        )) {
            log.error("乘客确认发车失败，订单状态不正确，订单id:{}", orderId);
            return R.error("乘客确认发车失败，订单状态不正确");
        }
        User user = userServiceClient.getUserByAccountUsername(loginAccount.getUsername());

        if (user == null) {
            log.error("按username查询用户失败，用户不存在，username:{}", loginAccount.getUsername());
            return R.error("用户不存在");
        }

        order.setStatus(OrderStatusEnum.DRIVING_USER_CONFIRM_DEPARTURE.getValue());
        boolean updateSuccess = updateById(order);
        if(updateSuccess){
            return R.ok("乘客确认发车成功");
        }else{
            log.error("乘客确认发车失败，订单id:{}",orderId);
            return R.error("乘客确认发车失败，MySQL数据库异常");
        }
    }

    @Override
    public R passengerConfirmArrived(Long orderId, LoginAccount loginAccount) {
        Order order = getById(orderId);

        if (order == null) {
            log.error("按orderID查询订单失败，orderAndUserExists，订单id:{}", orderId);
            return R.error("订单不存在");
        } else if (!order.getStatus().equals(
                OrderStatusEnum.DRIVING_USER_CONFIRM_DEPARTURE.getValue())) {
            log.error("乘客确认到达失败，订单状态不正确，订单id:{}", orderId);
            return R.error("乘客确认到达失败，订单状态不正确");
        }
        User user = userServiceClient.getUserByAccountUsername(loginAccount.getUsername());

        if (user == null) {
            log.error("按username查询用户失败，用户不存在，username:{}", loginAccount.getUsername());
            return R.error("用户不存在");
        }

        Carpooling carpooling = carpoolingServiceClient.getCarpoolingById(
                order.getCarpoolingId()
        );
        boolean updateSuccess;
        if (carpooling.getPrice() == 0){
            // 如果价格为0，说明是免费拼车，直接关闭订单
            order.setStatus(OrderStatusEnum.ORDER_NORMAL_CLOSED.getValue());
            updateSuccess = true;
        } else {
            order.setStatus(OrderStatusEnum.ARRIVED_USER_UNPAID.getValue());
            updateSuccess = updateById(order);

            // 乘客确认到达后，将该订单加入未完成订单表
            UnfinishedOrder unfinishedOrder = new UnfinishedOrder();
            unfinishedOrder.setCarpoolingId(carpooling.getId());
            unfinishedOrder.setOrderId(orderId);
            unfinishedOrder.setPassengerId(user.getId());
            updateSuccess = updateSuccess &&
                    unfinishedOrderMapper.insert(unfinishedOrder) > 0;
        }
        if(updateSuccess){
            return R.ok("乘客确认到达成功");
        }else{
            log.error("乘客确认到达失败，订单id:{}",orderId);
            return R.error("乘客确认到达失败，MySQL数据库异常");
        }
    }
}
