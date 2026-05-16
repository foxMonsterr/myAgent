package com.chat.myAgent.service.impl;

import com.chat.myAgent.auth.JwtProperties;
import com.chat.myAgent.auth.JwtTokenProvider;
import com.chat.myAgent.common.context.TraceContext;
import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.dto.LoginRequest;
import com.chat.myAgent.model.dto.RegisterRequest;
import com.chat.myAgent.model.entity.UserEntity;
import com.chat.myAgent.model.vo.AuthResponse;
import com.chat.myAgent.model.vo.SessionVO;
import com.chat.myAgent.repository.UserRepository;
import com.chat.myAgent.service.AuthService;
import com.chat.myAgent.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final SessionService sessionService;

    @Override
    @Transactional
    public R<AuthResponse> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return R.fail(400, "用户名已存在: " + request.getUsername());
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname())
                ? request.getNickname()
                : request.getUsername());
        user.setPhone(request.getPhone());
        user.setRole("USER");
        user.setEnabled(true);
        user.setPasswordUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("新用户注册: {}, traceId={}", user.getUsername(), TraceContext.getTraceId());

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole());
        saveSession(user, token);
        return R.ok("注册成功", buildAuthResponse(user, token));
    }

    @Override
    @Transactional
    public R<AuthResponse> login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return R.fail(401, "用户名或密码错误");
        }
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            return R.fail(403, "账号已被禁用");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole());
        saveSession(user, token);
        log.info("用户登录: {}, traceId={}", user.getUsername(), TraceContext.getTraceId());
        return R.ok("登录成功", buildAuthResponse(user, token));
    }

    @Override
    @Transactional
    public R<AuthResponse> initAdmin() {
        if (userRepository.existsByUsername("admin")) {
            return R.fail(400, "管理员账号已存在");
        }

        UserEntity admin = new UserEntity();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNickname("系统管理员");
        admin.setRole("ADMIN");
        admin.setEnabled(true);
        admin.setPasswordUpdatedAt(LocalDateTime.now());

        userRepository.save(admin);

        String token = jwtTokenProvider.generateToken(admin.getUsername(), admin.getRole());
        saveSession(admin, token);
        return R.ok("管理员账号创建成功", buildAuthResponse(admin, token));
    }

    @Override
    public R<Void> logout(String token) {
        if (StringUtils.hasText(token)) {
            sessionService.deleteSession(token);
        }
        return R.ok();
    }

    private void saveSession(UserEntity user, String token) {
        LocalDateTime now = LocalDateTime.now();
        SessionVO sessionVO = SessionVO.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .nickname(user.getNickname())
                .loginAt(now)
                .expiresAt(now.plusHours(jwtProperties.getExpirationHours()))
                .build();
        sessionService.saveSession(token, sessionVO);
    }

    private AuthResponse buildAuthResponse(UserEntity user, String token) {
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .nickname(user.getNickname())
                .expiresIn((long) jwtProperties.getExpirationHours() * 3600)
                .build();
    }
}
