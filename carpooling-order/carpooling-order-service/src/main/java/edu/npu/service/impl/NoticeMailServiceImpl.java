package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.npu.entity.Order;
import edu.npu.entity.User;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.OrderMapper;
import edu.npu.service.NoticeMailService;
import edu.npu.util.SendMailUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : [wangminan]
 * @description : [远程发信方法实现类]
 */
@Service
public class NoticeMailServiceImpl implements NoticeMailService {

    @Resource
    private SendMailUtil sendMailUtil;

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private OrderMapper orderMapper;

    @Override
    public void sendNoticeMailToUser(Long carpoolingId, String subject, String content) {
        List<Order> orders = orderMapper.selectList(
             new LambdaQueryWrapper<Order>()
                     .eq(Order::getCarpoolingId, carpoolingId)
        );
        // 遍历order获取User信息
        List<User> users = orders.stream().map(
                order -> userServiceClient.getUserById(order.getPassengerId())
        ).toList();
        // 遍历users发送邮件
        users.forEach(user -> sendMailUtil.sendMail(user.getEmail(), subject, content));
    }
}
