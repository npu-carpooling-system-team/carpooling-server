package edu.npu.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.npu.common.AlipayTradeState;
import edu.npu.common.OrderStatusEnum;
import edu.npu.entity.Carpooling;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.Order;
import edu.npu.entity.UnfinishedOrder;
import edu.npu.exception.CarpoolingError;
import edu.npu.exception.CarpoolingException;
import edu.npu.feignClient.CarpoolingServiceClient;
import edu.npu.mapper.OrderMapper;
import edu.npu.mapper.UnfinishedOrderMapper;
import edu.npu.service.OrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wangminan
 * @description 针对表【order(拼车订单表)】的数据库操作Service实现
 * @createDate 2023-04-25 11:27:12
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements OrderService {

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private Environment config;

    @Resource
    private CarpoolingServiceClient carpoolingServiceClient;

    @Resource
    private UnfinishedOrderMapper unfinishedOrderMapper;

    private static final String OUT_TRADE_NUMBER = "out_trade_no";

    private static final String CALL_SUCCESS = "调用成功,返回结果为==>";

    private static final String CALL_FAILURE = "调用失败,返回结果为==>";

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public String startPay(Long orderId, LoginAccount loginAccount) {
        log.debug("收到来自用户:{}对订单:{}的缴费请求,开始处理", loginAccount.getId(), orderId);
        // 调用支付宝接口 可能alt+enter没有候选项，需要自己写import
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();

        // TODO nacos配置的回调地址暂时配的是单体网关地址 最后肯定要用nginx做负载均衡
        request.setNotifyUrl(config.getProperty("alipay.notify-url"));
        request.setReturnUrl(config.getProperty("alipay.return-url"));

        // 以上是配置所有的公共参数，下面是配置请求参数集合
        ObjectNode bizContent = objectMapper.createObjectNode();
        bizContent.put(OUT_TRADE_NUMBER, orderId.toString());

        Carpooling carpooling = carpoolingServiceClient.getCarpoolingById(
                getById(orderId).getCarpoolingId()
        );
        bizContent.put("total_amount", carpooling.getPrice());
        bizContent.put("subject", "西工大拼车平台订单支付");
        // 手机端场景请使用QUICK_WAP_WAY 电脑FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "QUICK_WAP_WAY");
        bizContent.put("quit_url", config.getProperty("alipay.quit-url"));
        // 继续构造请求
        request.setBizContent(bizContent.toString());
        // 调用远程接口
        AlipayTradePagePayResponse response = null;
        try {
            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            log.error("创建支付交易失败");
            // try-catch与Transaction注解配合使用时需要手动抛出RuntimeException，否则事务不会回滚
            e.printStackTrace();
            CarpoolingException.cast(CarpoolingError.UNKNOWN_ERROR, "创建交易失败,未知异常");
        }
        if (response.isSuccess()) {
            log.info(CALL_SUCCESS + response.getBody());
        } else {
            log.info(CALL_FAILURE + response.getCode() + " " + response.getMsg());
            CarpoolingException.cast(CarpoolingError.UNKNOWN_ERROR, "创建支付交易失败,对方接口异常");
        }
        /*
            回跳地址示例
            https://m.alipay.com/Gk8NF23?total_amount=9.00
            &timestamp=2016-08-11+19%3A36%3A01
            &sign=ErCRRVmW%2FvXu1XO76k%2BUr4gYKC5%2FWgZGSo%2FR7nbL%2FPU7yFXtQJ2CjYPcqumxcYYB5x%2FzaRJXWBLN3jJXr01Icph8AZGEmwNuzvfezRoWny6%2Fm0iVQf7hfgn66z2yRfXtRSqtSTQWhjMa5YXE7MBMKFruIclYVTlfWDN30Cw7k%2Fk%3D
            &trade_no=2016081121001004630200142207
            &sign_type=RSA2
            &charset=UTF-8
            &seller_id=2088111111116894
            &method=alipay.trade.wap.pay.return
            &app_id=2016040501024706
            &out_trade_no=70501111111S001111119
            &version=1.0
         */
        return response.getBody();
    }

    @Override
    public String checkSignAndConfirm(Map<String, String> notifyParams) {
        /*
         * 回调示例如下:
         * 通知参数 ====>
         * {
         * gmt_create=2022-12-05 17:18:37,
         * charset=UTF-8,
         * gmt_payment=2022-12-05 17:18:46,
         * notify_time=2022-12-05 17:18:49,
         * subject=Java课程,
         * sign=FmKXT9HjUhWlbCImoG4zrTUD3fFGYAmAo+2bPBNJi2WqiGNzRHDJ7FT6EOpZULlWlQhg8dKfkJ5WtnxIMNYyoLx3g8t73MsWlzFRvfvT1lVAbzj4G69gs57bOPrE5wuJq6gDpWJ4wHDgMPUTABbulHzy0IGcsEAwdrCmtzmt5Jjrube2HgDxm84LIDzbilxl1TVoCVsxaLJj1WAZ2jmVxm2VcM0h9+e8bu/NacRZktofA5DpC7SYf03TMjWLRc4ih5EYGz2tAX9nt3i1LVyxmlJpvPuMobXgPMRcuzNAqDxcMo/CUEvIVmaDT4iiIU4qgPZ1sqAdrs67mFQep6VZ8A==,
         * buyer_id=2088722003402347,
         * invoice_amount=0.01,
         * version=1.0,
         * notify_id=2022120500222171848002340521336546,
         * fund_bill_list=[{"amount":"0.01","fundChannel":"ALIPAYACCOUNT"}],
         * notify_type=trade_status_sync,
         * out_trade_no=ORDER_20221205171809023,
         * total_amount=0.01,
         * trade_status=TRADE_SUCCESS,
         * trade_no=2022120522001402340501695232,
         * auth_app_id=2021000121691518,
         * receipt_amount=0.01,
         * point_amount=0.00,
         * app_id=2021000121691518,
         * buyer_pay_amount=0.01,
         * sign_type=RSA2,
         * seller_id=2088621993877332
         * }
         */
        String result = "failure";
        // 1.验签 如果成功返回success，否则返回failure(支付宝要求)
        boolean signVerified = false; //调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(
                    notifyParams,
                    config.getProperty("alipay.alipay-public-key"),
                    AlipayConstants.CHARSET_UTF8,
                    AlipayConstants.SIGN_TYPE_RSA2);
        } catch (AlipayApiException e) {
            CarpoolingException.cast(CarpoolingError.PARAMS_ERROR, "异步通知验签参数转换异常");
        }
        if (signVerified) {
            /*
                验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，
                 校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            */
            /*
             * 程序执行完后必须打印输出“success”（不包含引号）。
             * 如果商家反馈给支付宝的字符不是 success 这7个字符，支付宝服务器会不断重发通知，直到超过 24 小时 22 分钟。
             * 一般情况下，25 小时以内完成 8 次通知()
             */
            log.info("支付宝异步通知验签成功");
            /*
             * 进一步校验以下4项业务参数
             * 1.商家需要验证该通知数据中的 out_trade_no 是否为商家系统中创建的订单号。
             * 2.判断 total_amount 是否确实为该订单的实际金额（即商家订单创建时的金额）。
             * 3.校验通知中的 seller_id（或者 seller_email) 是否为 out_trade_no 这笔单据的对应的操作方
             * （有的时候，一个商家可能有多个 seller_id/seller_email）。
             * 4.验证 app_id 是否为该商家本身。
             */

            // out_trade_no
            String outTradeNo = notifyParams.get(OUT_TRADE_NUMBER);
            // 获取订单对象
            Order order = getById(outTradeNo);


            if (order == null) {
                log.error("支付宝给出的订单不存在，订单号：{}", outTradeNo);
                return result;
            }

            // total_amount
            String totalAmount = notifyParams.get("total_amount");
            // total_amount中的金额以元为单位，需要转换为分(数据库中记录的单位为分)
            int totalAmountInt = new BigDecimal(totalAmount).intValue();
            Carpooling thisCarpooling = carpoolingServiceClient
                                            .getCarpoolingById(order.getCarpoolingId());
            int originalPrice = thisCarpooling.getPrice();
            if ( originalPrice !=totalAmountInt){
                log.error("订单金额不一致，订单号：{}，订单金额：{}，支付宝金额：{}",
                        outTradeNo, originalPrice, totalAmountInt);
                return result;
            }

            // seller_id
            String sellerId = notifyParams.get("seller_id");
            if (!sellerId.equals(config.getProperty("alipay.seller-id"))) {
                log.error("商家pid不一致，订单号：{}，商家id：{}，支付宝商家id：{}",
                        outTradeNo, sellerId, config.getProperty("alipay.seller-id"));
                return result;
            }

            // app_id
            String appId = notifyParams.get("app_id");
            if (!appId.equals(config.getProperty("alipay.app-id"))) {
                log.error("app-id不一致，订单号：{}，商家app-id：{}，支付宝app-id：{}",
                        outTradeNo, appId, config.getProperty("alipay.app-id"));
                return result;
            }

            // 只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功。
            // 前者支持退款 后者不支持退款
            String tradeStatus = notifyParams.get("trade_status");
            if (!tradeStatus.equals(AlipayTradeState.SUCCESS.getType())) {
                log.error("交易状态不正确，订单号：{}，交易状态：{}", outTradeNo, tradeStatus);
                return result;
            }

            // 加锁,并发控制,防止重复回调
            /*
             * 注意：锁与@Transaction注解不兼容，如果使用了@Transaction注解，那么锁将失效
             * 1.如果使用了@Transaction注解，那么在方法执行完后，会自动提交事务，此时锁将自动释放
             * 2.如果没有使用@Transaction注解，那么在方法执行完后，需要手动提交事务，此时锁才会释放
             * 参考 https://blog.csdn.net/fal1230/article/details/113392123
             */
            if (lock.tryLock()) {
                try {
                    // 处理重复通知
                    // 无论接口被调用多少次 以下业务只执行一次
                    // 接口调用的幂等性
                    int orderStatus = order.getStatus();
                    if (orderStatus != OrderStatusEnum.ARRIVED_USER_UNPAID.getValue()) {
                        log.info("非到达订单，订单状态将不会被更新，订单号：{}", outTradeNo);
                        // if外将重新发送success
                    }

                    // 处理自身业务
                    log.info("处理订单");
                    // 修改订单状态 订单正常结束
                    order.setStatus(OrderStatusEnum.ORDER_NORMAL_CLOSED.getValue());
                    // 从unfinished order表中删除该条订单对应的记录
                    unfinishedOrderMapper.delete(
                            new LambdaQueryWrapper<UnfinishedOrder>()
                                    .eq(UnfinishedOrder::getOrderId, order.getId())
                    );
                    // 记录支付日志
                    log.info("订单号：{} 回调已收到，参数正常，状态已更新", outTradeNo);
                } finally {
                    lock.unlock();
                }
            }
            result = "success";
        } else {
            log.error("支付宝异步通知验签失败");
        }
        return result;
    }
}




