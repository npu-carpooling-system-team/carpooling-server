package edu.npu.controller;

import edu.npu.dto.AddMessageDto;
import edu.npu.entity.LoginAccount;
import edu.npu.service.ChatService;
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

    @PostMapping("/message")
    public R leaveMessage(@RequestBody @Validated AddMessageDto addMessageDto,
                          @AuthenticationPrincipal LoginAccount loginAccount) {
        return chatService.addMessage(addMessageDto, loginAccount);
    }

    @GetMapping("/message")
    public R getMessage(@AuthenticationPrincipal LoginAccount loginAccount) {
        return chatService.getMessage(loginAccount);
    }
}
