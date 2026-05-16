package com.chat.myAgent.service.impl;

import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.dto.SendCaptchaRequest;
import com.chat.myAgent.model.dto.VerifyCaptchaRequest;
import com.chat.myAgent.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private static final String CAPTCHA_KEY_PREFIX = "smartagent:auth:captcha:";
    private static final String CAPTCHA_FAIL_KEY_PREFIX = "smartagent:auth:captcha:fail:";
    private static final String CAPTCHA_COOLDOWN_KEY_PREFIX = "smartagent:auth:captcha:cooldown:";
    private static final long CAPTCHA_TTL_MINUTES = 5L;
    private static final long CAPTCHA_COOLDOWN_SECONDS = 60L;
    private static final int CAPTCHA_LENGTH = 4;
    private static final int MAX_FAIL_COUNT = 5;

    private final RedisTemplate<String, Object> redisTemplate;
    private final Random random = new Random();

    @Override
    public R<Void> sendCaptcha(SendCaptchaRequest request) {
        String cooldownKey = buildCooldownKey(request.getScene(), request.getPhone());
        Boolean isCoolingDown = redisTemplate.hasKey(cooldownKey);
        if (Boolean.TRUE.equals(isCoolingDown)) {
            return R.fail(429, "验证码发送过于频繁，请稍后再试");
        }

        String captcha = generateCaptcha();
        String captchaKey = buildCaptchaKey(request.getScene(), request.getPhone());
        String failKey = buildFailKey(request.getScene(), request.getPhone());

        redisTemplate.opsForValue().set(captchaKey, captcha, Duration.ofMinutes(CAPTCHA_TTL_MINUTES));
        redisTemplate.delete(failKey);
        redisTemplate.opsForValue().set(cooldownKey, "1", CAPTCHA_COOLDOWN_SECONDS, TimeUnit.SECONDS);

        // 临时方案：验证码直接写入日志，后续可替换为短信发送
        return R.ok();
    }

    @Override
    public R<Boolean> verifyCaptcha(VerifyCaptchaRequest request) {
        String captchaKey = buildCaptchaKey(request.getScene(), request.getPhone());
        String failKey = buildFailKey(request.getScene(), request.getPhone());
        Object storedCaptcha = redisTemplate.opsForValue().get(captchaKey);

        if (storedCaptcha == null) {
            return R.fail(400, "验证码已过期或不存在");
        }

        if (!request.getCaptcha().equals(String.valueOf(storedCaptcha))) {
            Long failCount = redisTemplate.opsForValue().increment(failKey);
            if (failCount != null && failCount == 1L) {
                redisTemplate.expire(failKey, Duration.ofMinutes(CAPTCHA_TTL_MINUTES));
            }
            if (failCount != null && failCount >= MAX_FAIL_COUNT) {
                redisTemplate.delete(captchaKey);
                return R.fail(429, "验证码错误次数过多，请重新获取验证码");
            }
            return R.fail(400, "验证码错误");
        }

        redisTemplate.delete(captchaKey);
        redisTemplate.delete(failKey);
        return R.ok(true);
    }

    private String generateCaptcha() {
        int code = random.nextInt(9000) + 1000;
        return String.valueOf(code);
    }

    private String buildCaptchaKey(String scene, String phone) {
        return CAPTCHA_KEY_PREFIX + scene + ":" + phone;
    }

    private String buildFailKey(String scene, String phone) {
        return CAPTCHA_FAIL_KEY_PREFIX + scene + ":" + phone;
    }

    private String buildCooldownKey(String scene, String phone) {
        return CAPTCHA_COOLDOWN_KEY_PREFIX + scene + ":" + phone;
    }
}
