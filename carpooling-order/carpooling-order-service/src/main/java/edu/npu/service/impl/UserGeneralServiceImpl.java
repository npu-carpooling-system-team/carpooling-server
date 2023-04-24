package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.Order;
import edu.npu.entity.User;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.OrderMapper;
import edu.npu.service.UserGeneralService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : [wangminan]
 * @description : [用户通用服务实现类]
 */
@Service
public class UserGeneralServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements UserGeneralService {

    @Resource
    private UserServiceClient userServiceClient;

    @Override
    public R getOrderList(LoginAccount loginAccount) {
        User user = userServiceClient
                .getUserByAccountUsername(loginAccount.getUsername());
        List<Order> orders = list(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getPassengerId, user.getId())
        );
        Map<String, Object> result = new HashMap<>();
        result.put("list", orders);
        return R.ok(result);
    }

    @Override
    public R getOrderById(Long orderId) {
        Order order = getById(orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("result", order);
        return R.ok(result);
    }
}
