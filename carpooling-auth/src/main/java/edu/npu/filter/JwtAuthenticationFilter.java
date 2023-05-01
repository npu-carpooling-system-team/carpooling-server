package edu.npu.filter;

import edu.npu.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@Component
@RequiredArgsConstructor // 作用于类，用于生成包含 final 和 @NonNull 注解的成员变量的构造方法 自动执行构造器注入
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            // 加上 @NonNull 注解，可以让 IDEA 不提示参数不能为空
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        // 该模块没有需要鉴权的接口
        filterChain.doFilter(request, response);
    }
}
