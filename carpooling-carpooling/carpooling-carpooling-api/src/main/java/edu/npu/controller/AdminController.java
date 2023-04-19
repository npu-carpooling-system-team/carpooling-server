package edu.npu.controller;

import edu.npu.common.ResponseCodeEnum;
import edu.npu.common.RoleEnum;
import edu.npu.entity.LoginAccount;
import edu.npu.service.AdminCarpoolingService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : [wangminan]
 * @description : [用于给管理员导出表格的控制器]
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminCarpoolingService adminCarpoolingService;

    @GetMapping("/drivers")
    @PreAuthorize("hasAuthority('admin')")
    public R getDriverList(@AuthenticationPrincipal LoginAccount loginAccount){
        // preAuthorize不知道为什么不生效 校验身份
        RoleEnum role = RoleEnum.fromValue(loginAccount.getRole());
        if (role == null || role.equals(RoleEnum.User)) {
            return R.error(ResponseCodeEnum.Forbidden, "权限不足");
        }
        return adminCarpoolingService.getDriverList();
    }
}
