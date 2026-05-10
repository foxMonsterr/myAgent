package com.chat.myAgent.model.platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 统一任务上下文
 *
 * 用于承载规划、执行、回退等任务状态，后续可扩展成工作流基础对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskContext {

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 所属会话 ID
     */
    private String conversationId;

    /**
     * 任务标题或摘要
     */
    private String summary;

    /**
     * 原始用户输入
     */
    private String input;

    /**
     * 当前任务状态，例如 PLANNING / RUNNING / SUCCESS / FAILED
     */
    private String status;

    /**
     * 是否自动执行规划结果
     */
    private boolean autoExecute;

    /**
     * 规划步骤描述列表
     */
    private List<String> steps;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
