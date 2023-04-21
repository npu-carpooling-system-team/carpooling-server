package edu.npu.controller;

import edu.npu.dto.AddMessageDto;
import edu.npu.entity.LoginAccount;
import edu.npu.service.ChatService;
import edu.npu.service.PreOrderService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@RestController
@RequestMapping("/preorder")
public class PreOrderController {

    @Resource
    private ChatService chatService;

    @Resource
    private PreOrderService preOrderService;

    @PostMapping("/message")
    public R leaveMessage(@RequestBody @Validated AddMessageDto addMessageDto,
                          @AuthenticationPrincipal LoginAccount loginAccount) {
        return chatService.addMessage(addMessageDto, loginAccount);
    }

    @GetMapping("/message")
    public R getMessage(@AuthenticationPrincipal LoginAccount loginAccount) {
        return chatService.getMessage(loginAccount);
    }

    /**
     * 乘客申请拼车
     * @param carpoolingId 拼车id
     * @param loginAccount 当前登录的用户账号
     * @return R
     */
    @PostMapping("/passenger/apply/{carpoolingId}")
    public R passengerApply(@PathVariable("carpoolingId") Long carpoolingId,
                            @AuthenticationPrincipal LoginAccount loginAccount) {
        return preOrderService.passengerApply(carpoolingId, loginAccount);
    }

    /**
     * 司机审核并确认是否通过订单
     * @param carpoolingId 拼车行程唯一编号
     * @param loginAccount 当前登录的用户账号
     * @return 返回R
     */
    @GetMapping("/driver/apply/list")
    public R driverConfirm(@RequestParam("carpoolingId") Long carpoolingId,
                           @AuthenticationPrincipal LoginAccount loginAccount){
        return preOrderService.driverGetConfirmList(carpoolingId, loginAccount);
    }
}
