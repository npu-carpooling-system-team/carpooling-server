package edu.npu.controller;

import edu.npu.dto.UserRegisterDto;
import edu.npu.service.LoginAccountService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : [wangminan]
 * @description : [用于处理基础注册、用户名密码登录、退出的接口]
 */
@RestController
public class BasicAuthController {

    @Resource
    private LoginAccountService loginAccountService;

    @PostMapping("/register/user")
    public R register(@Validated UserRegisterDto userRegisterDto){
        return loginAccountService.registerUser(userRegisterDto);
    }
}
