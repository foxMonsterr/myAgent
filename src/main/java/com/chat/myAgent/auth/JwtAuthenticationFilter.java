package com.chat.myAgent.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器
 *
 * 在每个请求到达 Controller 之前：
 * 1. 从 Header 中提取 Bearer Token
 * 2. 验证 Token 有效性
 * 3. 设置 Spring Security 上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 提取 Token
        String token = extractToken(request);

        // 2. 验证 Token 并设置安全上下文
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 从 Token 中提取用户信息
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            // 创建认证对象
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,                                    // 用户名
                            null,                                        // 密码（已认证，不需要）
                            List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))  // 权限
                    );

            // 设置到 Spring Security 上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("JWT认证通过: user={}, role={}", username, role);
        }

        // 3. 继续过滤器链（无论认证成功与否都要继续）
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求 Header 中提取 Token
     * 格式：Authorization: Bearer eyJhbGci...
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
