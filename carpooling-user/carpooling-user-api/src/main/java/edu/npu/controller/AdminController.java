package edu.npu.controller;

import edu.npu.service.DriverService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : [wangminan]
 * @description : [管理员控制器]
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private DriverService driverService;

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/driver/list")
    public R listDriver() {
        return driverService.genDriverSimpleList();
    }
}
