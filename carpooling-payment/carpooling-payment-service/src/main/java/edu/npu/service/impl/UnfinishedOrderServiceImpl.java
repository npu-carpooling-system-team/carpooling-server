package edu.npu.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.npu.common.OrderStatusEnum;
import edu.npu.entity.Order;
import edu.npu.entity.UnfinishedOrder;
import edu.npu.entity.User;
import edu.npu.exception.CarpoolingException;
import edu.npu.feignClient.CarpoolingServiceClient;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.UnfinishedOrderMapper;
import edu.npu.service.OrderService;
import edu.npu.service.UnfinishedOrderService;
import edu.npu.util.SendMailUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static edu.npu.common.AlipayRequestConstants.OUT_TRADE_NUMBER;

/**
* @author wangminan
* @description 针对表【unfinished_order】的数据库操作Service实现
* @createDate 2023-04-25 11:27:12
*/
@Service
@Slf4j
public class UnfinishedOrderServiceImpl extends ServiceImpl<UnfinishedOrderMapper, UnfinishedOrder>
    implements UnfinishedOrderService{

    @Resource
    @Lazy
    private UnfinishedOrderMapper unfinishedOrderMapper;

    @Resource
    private OrderService orderService;

    @Resource
    private SendMailUtil sendMailUtil;

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private CarpoolingServiceClient carpoolingServiceClient;

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private ObjectMapper objectMapper;

    private static final Integer ONE_DAY = 24 * 60 * 60 * 1000;

    private static final Long DEFAULT_PROCESS_MINUTES = 30L;

    @Override
    public void closeOrder(int shardIndex, int shardTotal, int count) {
        // 根据给出的分片索引和分片总数,计算出需要删除的聊天记录的id范围
        // 例如:分片索引为0,分片总数为2,则需要删除id为1,3,5,7,9...的聊天记录
        List<UnfinishedOrder> unfinishedOrders =
                unfinishedOrderMapper.selectListByShardIndex(shardIndex, shardTotal, count);
        // 查询对应的Order的update_time,如果超过1天,则发送通知要求支付
        // 如果超过2天,则关闭订单,同时拉黑用户
        List<Order> orderList =
                orderService.list(
                        new LambdaQueryWrapper<Order>()
                                .in(Order::getId,
                                        unfinishedOrders.stream().map(
                                                UnfinishedOrder::getOrderId
                                        ).toArray())
                );
        int size = orderList.size();
        log.info("取出:{}条数据,开始执行同步操作",size);

        //创建线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        //计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        for (Order order : orderList){
            threadPool.execute(() -> {
                User passenger = userServiceClient.getUserById(
                        order.getPassengerId()
                );
                // 先调用支付宝查单接口确认该订单确实没有支付
                if (tradeQuery(order.getId())) {
                    log.info("订单{}已经完成支付,删除未完成订单记录", order.getId());
                    // 已经完成支付 更新表格
                    order.setStatus(OrderStatusEnum.ORDER_NORMAL_CLOSED.getValue());
                    orderService.updateById(order);
                    // 向司机转账
                    boolean transfer = orderService.transfer(order);
                    if (!transfer) {
                        log.error("订单{}转账失败", order.getId());
                    } else {
                        log.info("订单{}转账成功", order.getId());
                    }
                    // 删除
                    remove(
                            new LambdaQueryWrapper<UnfinishedOrder>()
                                    .eq(UnfinishedOrder::getOrderId, order.getId())
                    );
                } else if (order.getUpdateTime().getTime() - System.currentTimeMillis() > ONE_DAY
                ) {
                    // 如果超过1天,则发送通知要求支付
                    boolean sendMailOneDay = sendMailUtil.sendMail(
                            passenger.getEmail(),
                            "西工大拼车平台订单超时未支付通知",
                            "您的订单" + order.getId() + "超时未支付,请尽快支付。逾期不支付您将被永久封禁。"
                    );
                    if (!sendMailOneDay) {
                        log.error("订单" + order.getId() + "超时未支付,发送邮件失败");
                    }
                } else if (
                        order.getUpdateTime().getTime() - System.currentTimeMillis() >
                                2L * ONE_DAY) {
                    // 如果超过两天
                    User driver = userServiceClient.getUserById(
                            carpoolingServiceClient.getCarpoolingById(
                                    order.getCarpoolingId()
                            ).getDriverId()
                    );
                    // 给Driver发邮件 很遗憾的通知他这笔钞票要不回来了
                    sendMailUtil.sendMail(
                            driver.getEmail(),
                            "西工大拼车平台订单超时未收款到账通知",
                            "很遗憾地通知您,您的订单" + order.getId() + "超时未收款到账,该乘客已被平台永久封禁。"
                    );
                    // 给乘客发邮件
                    sendMailUtil.sendMail(
                            passenger.getEmail(),
                            "西工大拼车平台订单超时未支付通知",
                            "很遗憾地通知您,您的订单" + order.getId() + "超时未支付,您已被平台永久封禁。"
                    );
                    // 将订单状态改为已关闭
                    order.setStatus(OrderStatusEnum.ORDER_FORCE_CLOSED.getValue());
                    // 删除UnfinishedOrder记录
                    remove(
                            new LambdaQueryWrapper<UnfinishedOrder>()
                                    .eq(UnfinishedOrder::getOrderId, order.getId())
                    );
                    // 永久封禁乘客
                    userServiceClient.banAccount(passenger);
                    log.info("订单{}超时未支付,已经永久封禁乘客{}", order.getId(), passenger.getId());
                }
            });
        }

        try {
            boolean await = countDownLatch.await(DEFAULT_PROCESS_MINUTES, TimeUnit.MINUTES);
            if (!await) {
                log.error("订单关闭任务超时");
            } else {
                log.info("订单关闭任务完成");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean tradeQuery(Long orderId){
        // 调用支付宝查单接口
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        ObjectNode bizContent = objectMapper.createObjectNode();
        bizContent.put(OUT_TRADE_NUMBER.getType(), String.valueOf(orderId));
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            throw new CarpoolingException("调用支付宝查单接口失败");
        }
        if (response.isSuccess()){
            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree(response.getBody());
            } catch (Exception e) {
                throw new CarpoolingException("解析支付宝查单接口返回的json失败");
            }
            JsonNode tradeStatus = jsonNode
                    .get("alipay_trade_query_response").get("trade_status");
            return tradeStatus.asText().equals("TRADE_SUCCESS");
        }
        return false;
    }
}
