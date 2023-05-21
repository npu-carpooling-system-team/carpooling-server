package edu.npu.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : [wangminan]
 * @description : [Feign配置类]
 */
@Configuration
public class FeignConfig {
    // 日志级别
    @Bean
    public feign.Logger.Level level() {
        return Logger.Level.FULL;
    }
}
