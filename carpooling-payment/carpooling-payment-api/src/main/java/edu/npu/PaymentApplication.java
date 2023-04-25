package edu.npu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author : [wangminan]
 * @description : 支付模块启动类
 */
@SpringBootApplication
public class PaymentApplication {

    public static void main(String[] args) {
        System.setProperty("nacos.logging.default.config.enabled","false");
        SpringApplication.run(PaymentApplication.class, args);
    }
}
