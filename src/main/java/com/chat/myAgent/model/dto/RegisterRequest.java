package com.chat.myAgent.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 5, max = 32, message = "用户名长度5-32个字符")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "用户名只能包含字母、数字、点、下划线和中划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度不能少于6个字符")
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?\\\\|`~]+$", message = "密码只能包含字母、数字和常用符号")
    private String password;

    /**
     * 昵称（可选）
     */
    private String nickname;

    /**
     * 手机号（可选，手机号注册时必填）
     */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 手机验证码（手机号注册时必填）
     */
    @Pattern(regexp = "^$|^\\d{4}$", message = "验证码必须为4位数字")
    private String captcha;

    /**
     * 注册方式：password / phone
     */
    @Pattern(regexp = "^(password|phone)$", message = "注册方式不正确")
    private String registerType = "password";
}
