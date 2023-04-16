package edu.npu.service;

import edu.npu.dto.CheckMailCodeDto;
import edu.npu.dto.CheckSmsCodeDto;
import edu.npu.dto.SendMailDto;
import edu.npu.dto.SendSmsDto;
import edu.npu.vo.R;

/**
 * @author : [wangminan]
 * @description : [负责处理短信和邮件的接口]
 */
public interface SmsAndMailService {
    R sendSms(SendSmsDto sendSmsDto);

    R sendMail(SendMailDto sendMailDto);

    R checkSmsCode(CheckSmsCodeDto checkSmsCodeDto);

    R checkMailCode(CheckMailCodeDto checkMailCodeDto);
}
