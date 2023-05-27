package edu.npu.config;

import io.lettuce.core.ReadFrom;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : [wangminan]
 * @description : [Redis配置类]
 */
@Configuration
public class RedisConfig {
    @Bean
    public LettuceClientConfigurationBuilderCustomizer customizer() {
        return clientConfigurationBuilder -> clientConfigurationBuilder
                // 优先读slave 所有slave节点都不可用才读master
                .readFrom(ReadFrom.REPLICA_PREFERRED);
    }
}
