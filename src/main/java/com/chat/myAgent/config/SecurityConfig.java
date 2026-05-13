package com.chat.myAgent.config;

import com.chat.myAgent.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
 * 说明：
 * - 项目采用 JWT 无状态认证，不依赖 Session
 * - 认证相关接口（登录 / 注册 / 初始化管理员）必须公开
 * - 文档类接口与静态资源需要公开，方便开发调试
 * - 知识库上传/加载、监控接口需要 ADMIN 角色
 * - 其余业务接口按需通过 JWT 认证
 *
 * 备注：
 * - 这里显式放行 OPTIONS 预检请求，避免浏览器 CORS 预检被 Security 拦截
 * - 这不是“端口放行”，而是“路径与方法放行”
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
                // 1) JWT 场景下关闭 CSRF，避免非浏览器表单场景的额外限制
                .csrf(AbstractHttpConfigurer::disable)

                // 2) 采用无状态会话，服务端不保存 Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3) 路径权限控制
                .authorizeHttpRequests(auth -> auth
                        // 浏览器 CORS 预检请求必须放行，否则前端可能直接看到 403
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 全局错误页放行，避免异常跳转时再次触发权限拦截
                        .requestMatchers("/error").permitAll()

                        // 认证接口：登录、注册、初始化管理员
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // 调试型公开接口：健康检查、快速对话
                        .requestMatchers("/api/v1/chat/ping").permitAll()
                        .requestMatchers("/api/v1/chat/quick").permitAll()

                        // 开发阶段为了便于前端联调，Chat / Agent / Planning 先开放
                        // 如果后续需要收紧权限，可在这里改成 authenticated()
                        .requestMatchers("/api/v1/agent/**").permitAll()
                        .requestMatchers("/api/v1/chat/**").permitAll()
                        .requestMatchers("/api/v1/planning/**").permitAll()
                        .requestMatchers("/api/v1/session/**").authenticated()

                        // 静态资源与首页放行
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**").permitAll()

                        // Knife4j / Swagger 文档放行，方便开发调试接口
                        .requestMatchers("/doc.html", "/webjars/**", "/swagger-resources/**", "/favicon.ico").permitAll()
                        // 如果你启用了 springdoc 标准 swagger，也一并放行
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // H2 控制台（开发环境）
                        .requestMatchers("/h2-console/**").permitAll()

                        // SSE 流式接口：为了方便浏览器直接测试，先放行
                        .requestMatchers("/api/v1/stream/**").permitAll()

                        // 知识库问答 / 检索 / 状态：登录后即可使用
                        .requestMatchers("/api/v1/knowledge/ask/**").authenticated()
                        .requestMatchers("/api/v1/knowledge/search").authenticated()
                        .requestMatchers("/api/v1/knowledge/status").authenticated()

                        // 知识库管理：上传 / 加载 需要管理员权限
                        .requestMatchers("/api/v1/knowledge/upload/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/knowledge/load-directory/**").hasRole("ADMIN")

                        // 文档列表 / 删除：普通用户和管理员都可以访问
                        .requestMatchers("/api/v1/knowledge/documents/**").hasAnyRole("ADMIN", "USER")

                        // 监控接口：仅管理员可访问
                        .requestMatchers("/api/v1/monitor/**").hasRole("ADMIN")

                        // 会话详情 / 历史查询：登录用户可访问，删除会话需要权限控制可在 Controller 上进一步收紧
                        .requestMatchers("/api/v1/session/**").authenticated()

                        // 其他所有接口：默认需要登录认证
                        .anyRequest().authenticated()
                )

                // 4) 在用户名密码认证过滤器之前加入 JWT 过滤器
                //    这样每个请求都会先尝试从 Authorization 头中解析 token
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 5) H2 控制台需要允许 iframe 同源访问
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
