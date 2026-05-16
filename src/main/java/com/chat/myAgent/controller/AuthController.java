package com.chat.myAgent.controller;

import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.dto.ForgotPasswordRequest;
import com.chat.myAgent.model.dto.LoginRequest;
import com.chat.myAgent.model.dto.RegisterRequest;
import com.chat.myAgent.model.dto.SendCaptchaRequest;
import com.chat.myAgent.model.dto.VerifyCaptchaRequest;
import com.chat.myAgent.model.vo.AuthResponse;
import com.chat.myAgent.service.AuthService;
import com.chat.myAgent.service.CaptchaService;
import com.chat.myAgent.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 *
 * 提供注册、登录功能
 * 所有接口公开，无需Token
 */
@Tag(name = "认证管理", description = "用户注册、登录、Token获取")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;
    private final PasswordService passwordService;

    /**
     * 用户注册
     * POST /api/v1/auth/register
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public R<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * 用户登录
     * POST /api/v1/auth/login
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * 创建管理员账号（仅供初始化使用）
     * POST /api/v1/auth/init-admin
     *
     * 生产环境应禁用此接口
     */
    @Operation(summary = "初始化管理员账号")
    @PostMapping("/init-admin")
    public R<AuthResponse> initAdmin() {
        return authService.initAdmin();
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        return authService.logout(token);
    }

    @Operation(summary = "发送验证码")
    @PostMapping("/captcha/send")
    public R<Void> sendCaptcha(@Valid @RequestBody SendCaptchaRequest request) {
        return captchaService.sendCaptcha(request);
    }

    @Operation(summary = "校验验证码")
    @PostMapping("/captcha/verify")
    public R<Boolean> verifyCaptcha(@Valid @RequestBody VerifyCaptchaRequest request) {
        return captchaService.verifyCaptcha(request);
    }

    @Operation(summary = "找回密码")
    @PostMapping("/password/forgot")
    public R<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return passwordService.forgotPassword(request);
    }
}
