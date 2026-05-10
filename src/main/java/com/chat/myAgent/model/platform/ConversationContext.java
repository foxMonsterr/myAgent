package com.chat.myAgent.model.platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一会话上下文
 *
 * 用于承载一次会话的通用信息，后续可扩展到：
 * - 请求追踪
 * - 会话状态管理
 * - 审计日志
 * - 前后端统一上下文传递
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationContext {

    /**
     * 会话 ID
     */
    private String conversationId;

    /**
     * 请求追踪 ID
     */
    private String traceId;

    /**
     * 发起人用户名
     */
    private String username;

    /**
     * 所属模块，例如 chat / agent / rag / planning
     */
    private String module;

    /**
     * 当前状态，例如 ACTIVE / CLOSED / ERROR
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;
}
