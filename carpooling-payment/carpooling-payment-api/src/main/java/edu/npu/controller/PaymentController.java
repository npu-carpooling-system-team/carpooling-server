package edu.npu.controller;

import edu.npu.entity.LoginAccount;
import edu.npu.service.OrderService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author : [wangminan]
 * @description : [支付接口]
 */
@RestController
@RequestMapping("/passenger/pay")
@Slf4j
public class PaymentController {

    @Resource
    private OrderService orderService;

    /**
     * 开始支付
     *
     * @param loginAccount 登录账号
     * @return R
     */
    @PostMapping("/{orderId}")
    public R startPay(@PathVariable("orderId") Long orderId,
                      @AuthenticationPrincipal LoginAccount loginAccount) {
        return orderService.startPay(orderId, loginAccount);
    }

    /**
     * 前端完成支付跳转后调用该接口
     * @param orderId 订单id
     * @param loginAccount 登录账号
     * @return R
     */
    @PutMapping("/{orderId}")
    public R updatePay(@PathVariable("orderId") Long orderId,
                      @AuthenticationPrincipal LoginAccount loginAccount) {
        return orderService.updatePay(orderId, loginAccount);
    }

    /**
     * 支付通知回调接口，该接口由支付宝开放平台调用，与前端无关
     * @param notifyParams 支付宝开放平台回调参数
     * @return 对支付宝开放平台响应校验后的结果
     */
    @PostMapping("/trade/notify")
    public String tradeNotify(@RequestParam Map<String, String> notifyParams){
        log.info("支付通知回调");
        log.info("通知参数 ====> {}", notifyParams);

        return orderService.checkSignAndConfirm(notifyParams);
    }
}
