package edu.npu.controller;

import edu.npu.dto.CheckMailCodeDto;
import edu.npu.dto.CheckSmsCodeDto;
import edu.npu.dto.SendMailDto;
import edu.npu.dto.SendSmsDto;
import edu.npu.service.SmsAndMailService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : [wangminan]
 * @description : [负责处理短信和邮件的接口]
 */
@RestController
public class SmsAndMailController {

    @Resource
    private SmsAndMailService smsAndMailService;

    @PostMapping("/sendsms")
    public R sendConfirmSms(@RequestBody @Validated SendSmsDto sendSmsDto){
        return smsAndMailService.sendSms(sendSmsDto);
    }

    @PostMapping("/checksms")
    public R checkSmsCode(@RequestBody @Validated CheckSmsCodeDto checkSmsCodeDto){
        return smsAndMailService.checkSmsCode(checkSmsCodeDto);
    }

    @PostMapping("/sendmail")
    public R sendConfirmMail(@RequestBody @Validated SendMailDto sendMailDto){
        return smsAndMailService.sendMail(sendMailDto);
    }

    @PostMapping("/checkmail")
    public R checkMailCode(@RequestBody @Validated CheckMailCodeDto checkMailCodeDto){
        return smsAndMailService.checkMailCode(checkMailCodeDto);
    }
}
