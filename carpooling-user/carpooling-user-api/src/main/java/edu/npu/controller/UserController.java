package edu.npu.controller;

import edu.npu.dto.PutUserInfoDto;
import edu.npu.entity.LoginAccount;
import edu.npu.service.UserService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [用户信息相关的请求]
 */
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @PreAuthorize("hasAuthority('User')")
    @GetMapping("/info")
    public R getUserInfo(@AuthenticationPrincipal LoginAccount loginAccount) {
        return userService.getInfo(loginAccount);
    }

    @PreAuthorize("hasAuthority('User')")
    @PutMapping("/info")
    public R updateUserInfo(@RequestBody @Validated PutUserInfoDto putUserInfoDto) {
        return userService.updateInfo(putUserInfoDto);
    }

    @PreAuthorize("hasAuthority('User')")
    @DeleteMapping("/info")
    public R deleteUserAccount(@AuthenticationPrincipal LoginAccount loginAccount) {
        return userService.deleteAccount(loginAccount);
    }
}
