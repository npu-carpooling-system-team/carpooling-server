package edu.npu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.npu.entity.Driver;
import edu.npu.entity.User;
import edu.npu.service.DriverService;
import edu.npu.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : [wangminan]
 * @description : [用于feign远程调用的接口]
 */
@RestController
@RequestMapping("/remote")
public class RemoteController {
    @Resource
    private UserService userService;

    @Resource
    private DriverService driverService;

    /**
     * 远程调用接口 通过用户名获取司机信息
     *
     * @param username 用户名
     * @return Driver
     */
    @GetMapping("/getDriver")
    public Driver getDriverByAccountUsername(
            @RequestParam(value = "username") String username
    ) {
        return userService.getDriverByAccountUsername(username);
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
    @GetMapping("/getUserByUsername")
    public User getUserWithAccountUsername(
            @RequestParam(value = "username") String username
    ) {
        return userService.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
    }

    /**
     * 远程调用接口 通过id获取用户信息
     * @param id 用户id
     * @return User
     */
    @GetMapping("/getUserById")
    public User getUserById(
            @RequestParam(value = "id") Long id
    ) {
        return userService.getById(id);
    }

    /**
     * 远程调用接口 通过id获取司机信息
     * @param driver 司机信息
     * @return boolean
     */
    @PutMapping("/updateDriver")
    public boolean updateDriver(@RequestBody Driver driver) {
        return driverService.updateById(driver);
    }

    @DeleteMapping("/ban")
    public boolean banAccount(@RequestBody User user){
        return userService.banUser(user);
    }
}
