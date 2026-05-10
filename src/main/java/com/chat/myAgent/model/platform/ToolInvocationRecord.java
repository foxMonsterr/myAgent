package com.chat.myAgent.model.platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 工具调用记录
 *
 * 用于记录一次 AI 工具调用的上下文、入参、出参和执行状态，方便审计和排障。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolInvocationRecord {

    /**
     * 调用记录 ID
     */
    private String invocationId;

    /**
     * 所属会话 ID
     */
    private String conversationId;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具入参
     */
    private Map<String, Object> input;

    /**
     * 工具输出
     */
    private Object output;

    /**
     * 执行状态，例如 SUCCESS / FAILED
     */
    private String status;

    /**
     * 异常信息
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 结束时间
     */
    private LocalDateTime finishedAt;
}
