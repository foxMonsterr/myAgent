package com.chat.myAgent.service;

import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.dto.UpdateProfileRequest;
import com.chat.myAgent.model.vo.UserProfileVO;

public interface UserProfileService {

    R<UserProfileVO> getProfile(String username);

    R<Void> updateProfile(String username, UpdateProfileRequest request);
}
