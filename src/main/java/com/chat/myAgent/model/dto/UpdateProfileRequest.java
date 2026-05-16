package com.chat.myAgent.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 100, message = "昵称长度不能超过100个字符")
    private String nickname;

    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
