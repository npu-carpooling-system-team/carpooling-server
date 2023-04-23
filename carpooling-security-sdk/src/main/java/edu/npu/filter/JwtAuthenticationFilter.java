package edu.npu.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.entity.LoginAccount;
import edu.npu.exception.CarpoolingException;
import edu.npu.util.JwtTokenProvider;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

import static edu.npu.common.RedisConstants.*;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            // 加上 @NonNull 注解，可以让 IDEA 不提示参数不能为空
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = getJwtFromRequest(request);
        final String username;
        if (StringUtils.hasText(authHeader)) {
            username = jwtTokenProvider.extractUsername(authHeader);
            // 用户未登录 所携带token为空 需要验证用户名密码后签发token
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 网关走过redis了 其他服务都可以不走 验证用户名密码
                // 从redis中获取用户信息
                Map<Object, Object> cachedUser = stringRedisTemplate
                        .opsForHash()
                        .entries(TOKEN_KEY_PREFIX + username);
                if (cachedUser.isEmpty() || cachedUser.get(HASH_TOKEN_KEY) == null
                        || !cachedUser.get(HASH_TOKEN_KEY).equals(authHeader)) {
                    CarpoolingException.cast("token已过期");
                }
                // 查map获取用户
                LoginAccount loginAccount = cachedUser.get(HASH_LOGIN_ACCOUNT_KEY) == null ? null :
                        objectMapper.convertValue(
                                cachedUser.get(HASH_LOGIN_ACCOUNT_KEY),
                                LoginAccount.class);
                if (loginAccount == null) {
                    CarpoolingException.cast("token已过期");
                }
                if (jwtTokenProvider.isTokenValid(authHeader, loginAccount)) {
                    // 将用户信息放入 SecurityContextHolder
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    loginAccount,
                                    null,
                                    loginAccount.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource()
                            .buildDetails(request));
                    /*
                     * SecurityContextHolder本身是一个ThreadLocal，无法被微服务中的其他服务访问到
                     * 因此我们需要所有请求都经过这样一个过滤器，将请求头中的token解析出来
                     * 然后将用户信息放入SecurityContextHolder
                     */
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    request.setAttribute("Authorization", "Bearer " + authHeader);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    // Bearer <token>
    private String getJwtFromRequest(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return "";
    }
}
