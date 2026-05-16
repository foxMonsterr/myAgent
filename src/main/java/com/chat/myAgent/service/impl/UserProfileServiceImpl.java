package com.chat.myAgent.service.impl;

import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.dto.UpdateProfileRequest;
import com.chat.myAgent.model.entity.UserEntity;
import com.chat.myAgent.model.vo.UserProfileVO;
import com.chat.myAgent.repository.UserRepository;
import com.chat.myAgent.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;

    @Override
    public R<UserProfileVO> getProfile(String username) {
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return R.fail(404, "用户不存在");
        }

        return R.ok(UserProfileVO.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build());
    }

    @Override
    @Transactional
    public R<Void> updateProfile(String username, UpdateProfileRequest request) {
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return R.fail(404, "用户不存在");
        }

        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        userRepository.save(user);
        return R.ok();
    }
}
