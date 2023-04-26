package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.OrderStatusEnum;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.Order;
import edu.npu.entity.User;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.OrderMapper;
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


    @Override
    public R passengerConfirmDeparture(Long orderId, LoginAccount loginAccount) {
        Order order = getById(orderId);

        if (order == null) {
            log.error("按orderID查询订单失败，订单不存在，订单id：{}", orderId);
            return R.error("订单不存在");
        }
        User user = userServiceClient.getUserByAccountUsername(loginAccount.getUsername());

        if (user == null) {
            log.error("按username查询用户失败，用户不存在，username：{}",loginAccount.getUsername() );
            return R.error("用户不存在");
        }
        order.setStatus(OrderStatusEnum.DRIVING_USER_CONFIRM_DEPARTURE.getValue());
        boolean updateSuccess = updateById(order);
        if(updateSuccess){
            return R.ok("乘客确认发车成功");
        }else{
            log.error("乘客确认发车失败，订单id：{}",orderId);
            return R.error("乘客确认发车失败，MySQL数据库异常");
        }
    }

    @Override
    public R passengerConfirmArrived(Long orderId, LoginAccount loginAccount) {
        Order order = getById(orderId);

        if (order == null) {
            log.error("按orderID查询订单失败，订单不存在，订单id：{}",orderId);
            return R.error("订单不存在");
        }
        User user = userServiceClient.getUserByAccountUsername(loginAccount.getUsername());

        if (user == null) {
            log.error("按username查询用户失败，用户不存在，username：{}",loginAccount.getUsername());
            return R.error("用户不存在");
        }
        order.setStatus(OrderStatusEnum.DRIVING_USER_CONFIRM_ARRIVED.getValue());
        boolean updateSuccess = updateById(order);
        if(updateSuccess){
            return R.ok("乘客确认到达成功");
        }else{
            log.error("乘客确认到达失败，订单id：{}",orderId);
            return R.error("乘客确认到达失败，MySQL数据库异常");
        }
    }
}
