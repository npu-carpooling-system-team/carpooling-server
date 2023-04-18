package edu.npu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@SpringBootApplication
public class AuthApplication {

        public static void main(String[] args) {
            System.setProperty("nacos.logging.default.config.enabled","false");
            SpringApplication.run(AuthApplication.class, args);
        }
}
