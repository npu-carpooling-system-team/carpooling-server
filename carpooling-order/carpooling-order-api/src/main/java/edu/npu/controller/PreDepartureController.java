package edu.npu.controller;

import edu.npu.entity.LoginAccount;
import edu.npu.service.PreDepartureOrderService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/predeparture")
public class PreDepartureController {

    @Resource
    private PreDepartureOrderService preDepartureOrderService;

    /**
     * 乘客取消订单
     * @param orderId 订单ID
     * @param loginAccount 登录用户
     * @return R
     */
    @DeleteMapping("/passenger/carpooling/{orderId}")
    public R userCancelOrder(@PathVariable("orderId") Long orderId,
                             @AuthenticationPrincipal LoginAccount loginAccount) {
        return preDepartureOrderService.userCancelOrder(orderId, loginAccount);
    }

    /**
     * 查询用户半年内取消订单的次数
     * @param loginAccount 登录用户
     * @return R
     */
    @GetMapping("/passenger/carpooling/canceltimes")
    public R searchUserCancelTimes(@AuthenticationPrincipal LoginAccount loginAccount) {
        return preDepartureOrderService.searchUserCancelTimes(loginAccount);
    }
}
