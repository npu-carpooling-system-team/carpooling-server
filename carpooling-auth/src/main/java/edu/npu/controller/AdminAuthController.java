package edu.npu.controller;

import edu.npu.service.AdminService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : [wangminan]
 * @description : [管理员在auth部分的业务处理]
 */
@RestController
@RequestMapping("/admin")
public class AdminAuthController {

    @Resource
    private AdminService adminService;

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/count")
    public R getLoginCount(){
        return adminService.getLoginCount();
    }
}
