package com.chat.myAgent.service.impl;

import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.dto.ForgotPasswordRequest;
import com.chat.myAgent.model.dto.ResetPasswordRequest;
import com.chat.myAgent.model.dto.VerifyCaptchaRequest;
import com.chat.myAgent.model.entity.UserEntity;
import com.chat.myAgent.repository.UserRepository;
import com.chat.myAgent.service.CaptchaService;
import com.chat.myAgent.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;

    @Override
    @Transactional
    public R<Void> forgotPassword(ForgotPasswordRequest request) {
        UserEntity user = userRepository.findByPhone(request.getPhone()).orElse(null);
        if (user == null) {
            return R.fail(404, "手机号未注册");
        }

        VerifyCaptchaRequest verifyRequest = new VerifyCaptchaRequest();
        verifyRequest.setPhone(request.getPhone());
        verifyRequest.setCaptcha(request.getCaptcha());
        verifyRequest.setScene("forgot");

        R<Boolean> captchaResult = captchaService.verifyCaptcha(verifyRequest);
        if (captchaResult.getCode() != 200) {
            return R.fail(captchaResult.getCode(), captchaResult.getMessage());
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return R.ok();
    }

    @Override
    @Transactional
    public R<Void> resetPassword(String username, ResetPasswordRequest request) {
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return R.fail(404, "用户不存在");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return R.fail(400, "旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return R.ok();
    }
}
