package com.chat.myAgent.model.platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 调用对象
 *
 * 用于统一描述一次 Agent 执行：输入、输出、模型、工具、状态等。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentInvocation {

    /**
     * 调用 ID
     */
    private String invocationId;

    /**
     * 会话 ID
     */
    private String conversationId;

    /**
     * Agent 类型
     */
    private String agentType;

    /**
     * 输入内容
     */
    private String input;

    /**
     * 输出内容
     */
    private String output;

    /**
     * 思考内容
     */
    private String thinking;

    /**
     * 使用模型
     */
    private ModelSelection modelSelection;

    /**
     * 工具调用记录列表
     */
    private List<ToolInvocationRecord> toolInvocations;

    /**
     * 运行状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
