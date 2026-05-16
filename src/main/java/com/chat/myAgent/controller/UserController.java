package com.chat.myAgent.controller;

import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.dto.ForgotPasswordRequest;
import com.chat.myAgent.model.dto.ResetPasswordRequest;
import com.chat.myAgent.model.dto.UpdatePhoneRequest;
import com.chat.myAgent.model.dto.UpdateProfileRequest;
import com.chat.myAgent.model.vo.UserProfileVO;
import com.chat.myAgent.service.PasswordService;
import com.chat.myAgent.service.PhoneService;
import com.chat.myAgent.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户管理", description = "个人信息、密码和手机号管理")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final PasswordService passwordService;
    private final PhoneService phoneService;
    private final UserProfileService userProfileService;

    @Operation(summary = "获取个人资料")
    @GetMapping("/profile")
    public R<UserProfileVO> getProfile(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return R.unauthorized("未登录");
        }
        return userProfileService.getProfile(authentication.getName());
    }

    @Operation(summary = "修改个人资料")
    @PutMapping("/profile")
    public R<Void> updateProfile(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        if (authentication == null || authentication.getName() == null) {
            return R.unauthorized("未登录");
        }
        return userProfileService.updateProfile(authentication.getName(), request);
    }

    @Operation(summary = "找回密码")
    @PutMapping("/password/forgot")
    public R<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return passwordService.forgotPassword(request);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public R<Void> resetPassword(Authentication authentication, @Valid @RequestBody ResetPasswordRequest request) {
        if (authentication == null || authentication.getName() == null) {
            return R.unauthorized("未登录");
        }
        return passwordService.resetPassword(authentication.getName(), request);
    }

    @Operation(summary = "修改手机号")
    @PutMapping("/phone")
    public R<Void> updatePhone(Authentication authentication, @Valid @RequestBody UpdatePhoneRequest request) {
        if (authentication == null || authentication.getName() == null) {
            return R.unauthorized("未登录");
        }
        return phoneService.updatePhone(authentication.getName(), request);
    }
}
