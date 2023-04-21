package edu.npu.util;

import edu.npu.exception.CarpoolingException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author : [wangminan]
 * @description : [使用spring-mail发送邮件]
 */
@Component
@Slf4j
public class SendMailUtil {

    @Value("${spring.mail.username}")
    private String sendMailer;

    @Resource
    private JavaMailSender javaMailSender;

    public boolean sendMail(String to, String subject, String content) {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom(sendMailer);
            simpleMailMessage.setTo(to);
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(content);
            //邮件发送时间
            simpleMailMessage.setSentDate(new Date());
            javaMailSender.send(simpleMailMessage);
            return true;
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            CarpoolingException.cast("邮件发送失败");
        }
        return false;
    }
}
