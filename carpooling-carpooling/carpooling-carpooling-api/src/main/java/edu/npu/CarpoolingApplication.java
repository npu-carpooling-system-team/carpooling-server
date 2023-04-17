package edu.npu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@SpringBootApplication
@EnableFeignClients(basePackages={"edu.npu.feignClient"})
public class CarpoolingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarpoolingApplication.class, args);
    }
}
