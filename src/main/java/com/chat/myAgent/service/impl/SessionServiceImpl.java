package com.chat.myAgent.service.impl;

import com.chat.myAgent.model.vo.SessionVO;
import com.chat.myAgent.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private static final String SESSION_KEY_PREFIX = "smartagent:auth:session:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveSession(String token, SessionVO sessionVO) {
        String key = buildKey(token);
        redisTemplate.opsForValue().set(key, sessionVO, Duration.ofHours(24));
    }

    @Override
    public SessionVO getSession(String token) {
        Object value = redisTemplate.opsForValue().get(buildKey(token));
        if (value instanceof SessionVO sessionVO) {
            return sessionVO;
        }
        if (value instanceof java.util.LinkedHashMap<?, ?> map) {
            SessionVO sessionVO = SessionVO.builder()
                    .token(String.valueOf(map.get("token")))
                    .username(String.valueOf(map.get("username")))
                    .role(String.valueOf(map.get("role")))
                    .nickname(String.valueOf(map.get("nickname")))
                    .build();
            return sessionVO;
        }
        return null;
    }

    @Override
    public void deleteSession(String token) {
        redisTemplate.delete(buildKey(token));
    }

    private String buildKey(String token) {
        return SESSION_KEY_PREFIX + token;
    }
}
