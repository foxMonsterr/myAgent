package com.chat.myAgent.model.platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一执行上下文
 *
 * 作为一次请求/一次 Agent 运行的临时上下文载体，适合保存：
 * - traceId
 * - conversationId
 * - 用户信息
 * - 模块名
 * - 扩展参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionContext {

    /**
     * 追踪 ID
     */
    private String traceId;

    /**
     * 会话 ID
     */
    private String conversationId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 模块名
     */
    private String module;

    /**
     * 额外扩展参数
     */
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();
}
