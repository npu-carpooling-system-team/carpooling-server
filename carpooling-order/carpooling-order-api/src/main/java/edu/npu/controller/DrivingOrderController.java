package edu.npu.controller;

import edu.npu.entity.LoginAccount;
import edu.npu.service.DrivingOrderService;
import jakarta.annotation.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import edu.npu.vo.R;

@RestController
@RequestMapping("/inprocess")
public class DrivingOrderController {

    @Resource
    private DrivingOrderService drivingOrderService;

    /**
     * 乘客确认发车
     * @param orderId
     * @param loginAccount
     * @return
     */
    @PostMapping("/passenger/departure/confirm/{orderId}")
    public R passengerConfirmDeparture(@PathVariable("orderId") Long orderId,
                                       @AuthenticationPrincipal LoginAccount loginAccount) {
        return drivingOrderService.passengerConfirmDeparture(orderId, loginAccount);
    }

    /**
     * 乘客确认到达
     * @param orderId
     * @param loginAccount
     * @return
     */
    @PostMapping("/passenger/arrive/confirm/{orderId}")
    public R passengerConfirmArrive(@PathVariable("orderId") Long orderId,
                                       @AuthenticationPrincipal LoginAccount loginAccount) {
        return drivingOrderService.passengerConfirmArrived(orderId, loginAccount);
    }
}
