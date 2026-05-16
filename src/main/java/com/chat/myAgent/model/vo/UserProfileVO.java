package com.chat.myAgent.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileVO {
    private String username;
    private String nickname;
    private String phone;
    private String role;
    private Boolean enabled;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
