package com.chat.myAgent.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendCaptchaRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "场景不能为空")
    @Pattern(regexp = "^(register|forgot|update-phone)$", message = "场景值不正确")
    private String scene;
}
