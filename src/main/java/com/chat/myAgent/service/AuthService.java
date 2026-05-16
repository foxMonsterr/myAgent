package com.chat.myAgent.service;

import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.dto.LoginRequest;
import com.chat.myAgent.model.dto.RegisterRequest;
import com.chat.myAgent.model.vo.AuthResponse;

public interface AuthService {

    R<AuthResponse> register(RegisterRequest request);

    R<AuthResponse> login(LoginRequest request);

    R<AuthResponse> initAdmin();

    R<Void> logout(String token);
}
