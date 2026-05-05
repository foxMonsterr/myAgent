package com.chat.myAgent.config;

import com.chat.myAgent.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 *
 * 设计说明：
 * - 使用 JWT 无状态认证，不使用 Session
 * - 公开接口：登录、注册、健康检查、Swagger、静态页面
 * - 知识库管理接口：需要 ADMIN 角色
 * - 其他 API：需要认证（USER 或 ADMIN）
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF（使用 JWT 无状态认证，不需要 CSRF）
                .csrf(AbstractHttpConfigurer::disable)

                // 无状态会话
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 路径权限配置
                .authorizeHttpRequests(auth -> auth
                        // 公开接口 - 无需认证
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/chat/ping").permitAll()
                        .requestMatchers("/api/v1/chat/quick").permitAll()

                        // Agent 相关接口 - 开发阶段放行（方便前端测试）
                        .requestMatchers("/api/v1/agent/**").permitAll()
                        .requestMatchers("/api/v1/chat/**").permitAll()
                        .requestMatchers("/api/v1/planning/**").permitAll()

                        // 静态资源 - 无需认证
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**").permitAll()

                        // Swagger 文档 - 无需认证
//                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // Knife4j 文档 - 无需认证
                        .requestMatchers("/doc.html", "/webjars/**", "/swagger-resources/**", "/favicon.ico").permitAll()

                        // H2 控制台（开发用）
                        .requestMatchers("/h2-console/**").permitAll()

                        // SSE 流式接口 - 无需认证（方便浏览器直接测试）
                        .requestMatchers("/api/v1/stream/**").permitAll()

                        // 知识库管理（上传、删除）- 需要 ADMIN 角色
                        .requestMatchers("/api/v1/knowledge/upload/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/knowledge/load-directory/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/knowledge/documents/**").hasAnyRole("ADMIN", "USER")

                        // 监控接口 - 需要 ADMIN 角色
                        .requestMatchers("/api/v1/monitor/**").hasRole("ADMIN")

                        // 其他所有 API - 需要认证
                        .anyRequest().authenticated()
                )

                // 添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 允许 H2 控制台的 iframe
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
