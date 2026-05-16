package com.chat.myAgent.service;

import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.dto.UpdatePhoneRequest;

public interface PhoneService {

    R<Void> updatePhone(String username, UpdatePhoneRequest request);
}
