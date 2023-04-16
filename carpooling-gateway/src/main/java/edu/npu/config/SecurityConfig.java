package edu.npu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * @description 安全配置类
 * @author wangminan
 */
@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

    /**
     * 安全拦截配置
     * 交换机放行一切接口，具体鉴权由 {@link GatewayAuthFilter} 过滤器实现
     */
    @Bean
    public SecurityWebFilterChain webFluxSecurityFilterChain(ServerHttpSecurity http) {
        return http.authorizeExchange()
                .pathMatchers("/**").permitAll()
                .anyExchange().authenticated()
                .and().csrf().disable().build();
    }
}
