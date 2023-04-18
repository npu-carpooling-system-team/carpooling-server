package edu.npu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author : [wangminan]
 * @description : [网关启动类]
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        System.setProperty("nacos.logging.default.config.enabled","false");
        SpringApplication.run(GatewayApplication.class, args);
    }
}
