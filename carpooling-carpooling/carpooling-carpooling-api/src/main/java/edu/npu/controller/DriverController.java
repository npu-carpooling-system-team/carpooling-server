package edu.npu.controller;

import edu.npu.dto.AddCarpoolingDto;
import edu.npu.entity.LoginAccount;
import edu.npu.service.CarpoolingService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : [wangminan]
 * @description : [司机CRUD拼车形成的Controller]
 */
@RestController
public class DriverController {

    @Resource
    private CarpoolingService carpoolingService;

    @PostMapping("/driver/carpooling")
    public R addCarpooling(@RequestBody @Validated AddCarpoolingDto addCarpoolingDto,
                           @AuthenticationPrincipal LoginAccount loginAccount) {
        return carpoolingService.addCarpooling(addCarpoolingDto, loginAccount);
    }

    @PutMapping("/driver/carpooling")
    public R updateCarpooling(@RequestBody @Validated AddCarpoolingDto addCarpoolingDto,
                              @AuthenticationPrincipal LoginAccount loginAccount) {
        return carpoolingService.updateCarpooling(addCarpoolingDto, loginAccount);
    }
}
