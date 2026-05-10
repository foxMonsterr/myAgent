package com.chat.myAgent.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话摘要视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionSummaryVO {
    private String conversationId;
    private String agentType;
    private String model;
    private String lastMessage;
    private int messageCount;
    private LocalDateTime lastUpdatedAt;
}
