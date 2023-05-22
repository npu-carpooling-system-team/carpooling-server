package edu.npu.service.impl;

import cn.hutool.core.util.RandomUtil;
import edu.npu.common.RedisConstants;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.CheckMailCodeDto;
import edu.npu.dto.CheckSmsCodeDto;
import edu.npu.dto.SendMailDto;
import edu.npu.dto.SendSmsDto;
import edu.npu.service.SmsAndMailService;
import edu.npu.util.SendMailUtil;
import edu.npu.util.SendSmsUtil;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author : [wangminan]
 * @description : [负责处理短信和邮件的接口实现类]
 */
@Service
public class SmsAndMailServiceImpl implements SmsAndMailService {

    @Resource
    private SendSmsUtil sendSmsUtil;

    @Resource
    private SendMailUtil sendMailUtil;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public R sendSms(SendSmsDto sendSmsDto) {
        String phone = sendSmsDto.phone();
        String checkCode = RandomUtil.randomNumbers(4);
        stringRedisTemplate.opsForValue().set(
                RedisConstants.SMS_CODE_PREFIX + phone,
                checkCode,
                RedisConstants.CODE_EXPIRE_TIME,
                TimeUnit.SECONDS
        );
        sendSmsUtil.sendSmsCode(phone, checkCode);
        return R.ok();
    }

    @Override
    public R sendMail(SendMailDto sendMailDto) {
        String to = sendMailDto.email();
        String checkCode = RandomUtil.randomNumbers(4);
        stringRedisTemplate.opsForValue().set(
                RedisConstants.MAIL_CODE_PREFIX + to,
                checkCode,
                RedisConstants.CODE_EXPIRE_TIME,
                TimeUnit.SECONDS
        );
        String subject = "西工大拼车平台验证码";
        String content = "您的验证码为:" + checkCode + "，请在5分钟内输入以验证您的邮箱。";
        boolean sendMailSuccess = sendMailUtil.sendMail(to, subject, content);
        return sendMailSuccess ? R.ok() : R.error(ResponseCodeEnum.SERVER_ERROR, "邮件发送失败");
    }

    @Override
    public R checkSmsCode(CheckSmsCodeDto checkSmsCodeDto) {
        String phone = checkSmsCodeDto.phone();
        String code = checkSmsCodeDto.code();
        String cachedCode = stringRedisTemplate.opsForValue().get(RedisConstants.SMS_CODE_PREFIX + phone);
        if (cachedCode == null){
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "验证码已过期");
        } else {
            if (cachedCode.equals(code)){
                return R.ok();
            } else {
                return R.error(ResponseCodeEnum.FORBIDDEN, "验证码错误");
            }
        }
    }

    @Override
    public R checkMailCode(CheckMailCodeDto checkMailCodeDto) {
        String email = checkMailCodeDto.mail();
        String code = checkMailCodeDto.code();
        String cachedCode = stringRedisTemplate.opsForValue().get(RedisConstants.MAIL_CODE_PREFIX + email);
        if (cachedCode == null){
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "验证码已过期");
        } else {
            if (cachedCode.equals(code)){
                return R.ok();
            } else {
                return R.error(ResponseCodeEnum.FORBIDDEN, "验证码错误");
            }
        }
    }
}
