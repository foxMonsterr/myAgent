package com.chat.myAgent.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SessionVO {
    private String token;
    private String username;
    private String role;
    private String nickname;
    private LocalDateTime loginAt;
    private LocalDateTime expiresAt;
}
