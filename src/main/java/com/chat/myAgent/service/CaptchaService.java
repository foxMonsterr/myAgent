package com.chat.myAgent.service;

import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.dto.SendCaptchaRequest;
import com.chat.myAgent.model.dto.VerifyCaptchaRequest;

public interface CaptchaService {

    R<Void> sendCaptcha(SendCaptchaRequest request);

    R<Boolean> verifyCaptcha(VerifyCaptchaRequest request);
}
