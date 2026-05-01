package com.chat.myAgent.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 聊天响应体
 */
@Data
@Builder
public class ChatResponse {

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * AI 回复内容
     */
    private String reply;

    /**
     * 使用的模型名称
     */
    private String model;

    /**
     * Token 使用情况（扩展性预留）
     */
    private TokenUsage tokenUsage;

    @Data
    @Builder
    public static class TokenUsage {
        private Long promptTokens;
        private Long completionTokens;
        private Long totalTokens;
    }
}
