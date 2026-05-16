package com.chat.myAgent.service;

import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.dto.ForgotPasswordRequest;
import com.chat.myAgent.model.dto.ResetPasswordRequest;

public interface PasswordService {

    R<Void> forgotPassword(ForgotPasswordRequest request);

    R<Void> resetPassword(String username, ResetPasswordRequest request);
}
