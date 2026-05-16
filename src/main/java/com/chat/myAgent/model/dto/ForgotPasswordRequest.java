package com.chat.myAgent.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{4}$", message = "验证码必须为4位数字")
    private String captcha;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度不能少于6个字符")
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?\\\\|`~]+$", message = "密码只能包含字母、数字和常用符号")
    private String newPassword;
}
