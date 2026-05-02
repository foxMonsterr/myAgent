package com.chat.myAgent.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 任务规划响应体
 */
@Data
@Builder
public class PlanningResponse {

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 任务总结
     */
    private String taskSummary;

    /**
     * 是否进行了任务规划（简单问题直接回答则为false）
     */
    private boolean planned;

    /**
     * 如果未规划，直接回答的内容
     */
    private String directAnswer;

    /**
     * 执行步骤及结果
     */
    private List<StepResult> steps;

    /**
     * 最终汇总回答
     */
    private String finalAnswer;

    /**
     * 总耗时（毫秒）
     */
    private long totalTimeMs;
}
