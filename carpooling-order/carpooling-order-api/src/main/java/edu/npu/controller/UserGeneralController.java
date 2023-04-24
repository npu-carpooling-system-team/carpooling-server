package edu.npu.controller;

import edu.npu.entity.LoginAccount;
import edu.npu.service.UserGeneralService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : [wangminan]
 * @description : [乘客通用Controller]
 */
@RestController
@RequestMapping("/order")
public class UserGeneralController {

    @Resource
    private UserGeneralService userGeneralService;

    @GetMapping("/list")
    public R getOrderList(@AuthenticationPrincipal LoginAccount loginAccount) {
        return userGeneralService.getOrderList(loginAccount);
    }

    @GetMapping
    public R getOrder(@RequestParam("orderId") Long orderId) {
        return userGeneralService.getOrderById(orderId);
    }
}
