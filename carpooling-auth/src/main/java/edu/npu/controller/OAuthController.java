package edu.npu.controller;

import edu.npu.dto.AlipayLoginCallbackDto;
import edu.npu.service.LoginAccountService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author : [wangminan]
 * @description : [用于处理第三方用户登录的接口]
 */
@Controller // 返回需要是URL或者页面 不能REST
public class OAuthController {

    @Resource
    private LoginAccountService loginAccountService;

    @GetMapping("/login/oauth/callback")
    public String callback(AlipayLoginCallbackDto alipayLoginCallbackDto) {
        return loginAccountService.handleAlipayLogin(alipayLoginCallbackDto);
    }
}
