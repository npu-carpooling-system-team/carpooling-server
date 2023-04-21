package edu.npu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.npu.dto.PutUserInfoDto;
import edu.npu.entity.Driver;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.User;
import edu.npu.service.DriverService;
import edu.npu.service.UserService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : [wangminan]
 * @description : [用户信息相关的请求]
 */
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private DriverService driverService;

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

    /**
     * 远程调用接口 通过用户名获取司机信息
     *
     * @param username 用户名
     * @return Driver
     */
    @GetMapping("/getDriver")
    public Driver getDriverWithAccountUsername(
            @RequestParam("username") String username
    ) {
        return userService.getDriverWithAccountUsername(username);
    }

    /**
     * 远程调用接口 获取司机列表
     *
     * @return List<Driver>
     */
    @GetMapping("/getDriverList")
    public List<Driver> getDriverList() {
        return driverService.list();
    }

    /**
     * 远程调用接口 通过用户名获取用户信息
     * @param username 用户名
     * @return User
     */
    @GetMapping("/getUser")
    public User getUserWithAccountUsername(
            @RequestParam("username") String username
    ) {
        return userService.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
    }

    @GetMapping("/getUser/{id}")
    public User getUserById(
            @PathVariable("id") Long id
    ) {
        return userService.getById(id);
    }
}
