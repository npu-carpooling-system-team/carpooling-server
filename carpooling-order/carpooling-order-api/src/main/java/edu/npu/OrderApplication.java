package edu.npu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author : [wangminan]
 * @description : [订单业务处理模块]
 */
@SpringBootApplication
@EnableFeignClients(basePackages={"edu.npu.feignClient"})
public class OrderApplication {
    public static void main(String[] args) {
        System.setProperty("nacos.logging.default.config.enabled","false");
        SpringApplication.run(OrderApplication.class);
    }
}
