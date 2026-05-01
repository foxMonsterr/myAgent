package com.chat.myAgent.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 聊天请求体
 */
@Data
public class ChatRequest {

    /**
     * 会话ID（用于多轮对话隔离，阶段1可选，阶段2必填）
     */
    private String conversationId;

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 指定模型（可选，不传则用默认配置）
     * 扩展性预留：后续支持动态切换模型
     */
    private String model;
}
