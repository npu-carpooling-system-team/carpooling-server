package edu.npu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author : [wangminan]
 * @description : 支付模块启动类
 */
@SpringBootApplication
@EnableFeignClients(basePackages={"edu.npu.feignClient"})
public class PaymentApplication {

    public static void main(String[] args) {
        System.setProperty("nacos.logging.default.config.enabled","false");
        SpringApplication.run(PaymentApplication.class, args);
    }
}
