package edu.npu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.npu.common.OrderStatusEnum;
import edu.npu.entity.Order;
import edu.npu.mapper.OrderMapper;
import edu.npu.service.NoticeMailService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : [wangminan]
 * @description : [远程发信Controller 内部方法 仅供feign调用]
 */
@RestController
@RequestMapping("/remote")
public class RemoteController {

    @Resource
    private NoticeMailService noticeMailService;

    @Resource
    private OrderMapper orderMapper;

    @GetMapping("/mail")
    public void sendNoticeMailToUser(
            @RequestParam(value = "carpoolingId") Long carpoolingId,
            @RequestParam(value = "subject") String subject,
            @RequestParam(value = "content") String content
    ) {
        noticeMailService.sendNoticeMailToUser(carpoolingId, subject, content);
    }

    @PutMapping
    public void forceCloseOrderByCarpoolingId(
            @RequestParam(value = "carpoolingId") Long carpoolingId) {
        List<Order> orderList = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getCarpoolingId, carpoolingId)
        );
        orderList.forEach(order -> {
            order.setStatus(OrderStatusEnum.ORDER_FORCE_CLOSED.getValue());
            orderMapper.updateById(order);
        });
    }

    @GetMapping
    public boolean checkHasPassenger(
            @RequestParam(value = "carpoolingId") Long carpoolingId
    ) {
        List<Order> orderList = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getCarpoolingId, carpoolingId)
        );
        return !orderList.isEmpty();
    }
}
