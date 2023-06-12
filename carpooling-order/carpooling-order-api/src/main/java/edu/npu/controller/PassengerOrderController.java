package edu.npu.controller;

import edu.npu.entity.LoginAccount;
import edu.npu.service.DrivingOrderService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inprocess")
public class PassengerOrderController {

    @Resource
    private DrivingOrderService drivingOrderService;

    /**
     * 乘客确认发车
     * @param orderId 订单ID
     * @param loginAccount 登录账号
     * @return R
     */
    @PostMapping("/passenger/departure/confirm/{orderId}")
    public R passengerConfirmDeparture(@PathVariable("orderId") Long orderId,
                                       @AuthenticationPrincipal LoginAccount loginAccount) {
        return drivingOrderService.passengerConfirmDeparture(orderId, loginAccount);
    }

    /**
     * 乘客确认到达
     * @param orderId 订单ID
     * @param loginAccount 登录账号
     * @return R
     */
    @PostMapping("/passenger/arrive/confirm/{orderId}")
    public R passengerConfirmArrive(@PathVariable("orderId") Long orderId,
                                       @AuthenticationPrincipal LoginAccount loginAccount) {
        return drivingOrderService.passengerConfirmArrived(orderId, loginAccount);
    }
}
