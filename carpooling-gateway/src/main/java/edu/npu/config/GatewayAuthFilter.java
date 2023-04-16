package edu.npu.config;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.entity.LoginAccount;
import edu.npu.util.JwtTokenProvider;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static edu.npu.common.RedisConstants.TOKEN_EXPIRE_TTL;
import static edu.npu.common.RedisConstants.TOKEN_KEY_PREFIX;

/**
 * @author : [wangminan]
 * @description : [网关过滤器，负责全局鉴权]
 */
@Configuration
@Slf4j
public class GatewayAuthFilter implements GlobalFilter, Ordered {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    @Lazy
    private ObjectMapper objectMapper;

    //白名单
    private static List<String> whitelist = null;

    static {
        //加载白名单
        try (
                InputStream resourceAsStream = GatewayAuthFilter.class.getResourceAsStream("/security-whitelist.properties");
        ) {
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            Set<String> strings = properties.stringPropertyNames();
            whitelist= new ArrayList<>(strings);

        } catch (Exception e) {
            log.error("加载/security-whitelist.properties出错:{}",e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestUrl = exchange.getRequest().getPath().value();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        //白名单放行
        for (String url : whitelist) {
            if (pathMatcher.match(url, requestUrl)) {
                return chain.filter(exchange);
            }
        }
        //检查token是否存在
        String token = getTokenFromHeader(exchange);
        if (StrUtil.isBlank(token)) {
            return buildReturnMono("您需经认证方可访问",exchange);
        }
        String username = jwtTokenProvider.extractUsername(token);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 从Redis中获取user信息
            String loginAccountStr = stringRedisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + username);
            if (loginAccountStr == null) {
                return buildReturnMono("认证令牌无效",exchange);
            }
            LoginAccount loginAccount =
                    objectMapper.convertValue(
                            loginAccountStr, LoginAccount.class);
            // 校验令牌合法性 是否过期
            if (jwtTokenProvider.isTokenValid(token, loginAccount)) {
                // 重新在redis中设置过期时间以使令牌延期
                stringRedisTemplate.opsForValue().set(
                        TOKEN_KEY_PREFIX + username, loginAccountStr,
                        TOKEN_EXPIRE_TTL, TimeUnit.MILLISECONDS);
                // 将用户信息放入 SecurityContextHolder
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                loginAccount,
                                null,
                                loginAccount.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
                return chain.filter(exchange);
            }
        }
        return buildReturnMono("认证令牌无效",exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 获取token
     */
    private String getTokenFromHeader(ServerWebExchange exchange) {
        String tokenStr = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StrUtil.isBlank(tokenStr)) {
            return null;
        }
        String token = tokenStr.split(" ")[1];
        if (StrUtil.isBlank(token)) {
            return null;
        }
        return token;
    }

    private Mono<Void> buildReturnMono(String error, ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(new RestErrorResponse(error));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        byte[] bits = jsonString.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }
}
