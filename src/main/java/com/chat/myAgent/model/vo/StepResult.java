package com.chat.myAgent.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 单步执行结果
 */
@Data
@Builder
public class StepResult {

    /**
     * 步骤编号
     */
    private int stepNumber;

    /**
     * 步骤描述
     */
    private String description;

    /**
     * 使用的工具
     */
    private String toolUsed;

    /**
     * 执行结果
     */
    private String result;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 耗时（毫秒）
     */
    private long timeMs;
}
