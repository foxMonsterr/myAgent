package com.chat.myAgent.service.impl;

import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.dto.UpdatePhoneRequest;
import com.chat.myAgent.model.dto.VerifyCaptchaRequest;
import com.chat.myAgent.model.entity.UserEntity;
import com.chat.myAgent.repository.UserRepository;
import com.chat.myAgent.service.CaptchaService;
import com.chat.myAgent.service.PhoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhoneServiceImpl implements PhoneService {

    private final UserRepository userRepository;
    private final CaptchaService captchaService;

    @Override
    @Transactional
    public R<Void> updatePhone(String username, UpdatePhoneRequest request) {
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return R.fail(404, "用户不存在");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            return R.fail(400, "手机号已存在");
        }

        VerifyCaptchaRequest verifyRequest = new VerifyCaptchaRequest();
        verifyRequest.setPhone(request.getPhone());
        verifyRequest.setCaptcha(request.getCaptcha());
        verifyRequest.setScene("update-phone");

        R<Boolean> captchaResult = captchaService.verifyCaptcha(verifyRequest);
        if (captchaResult.getCode() != 200) {
            return R.fail(captchaResult.getCode(), captchaResult.getMessage());
        }

        user.setPhone(request.getPhone());
        userRepository.save(user);
        return R.ok();
    }
}
