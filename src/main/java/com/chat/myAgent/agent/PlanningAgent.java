package com.chat.myAgent.agent;

import com.chat.myAgent.model.vo.PlanningResponse;
import com.chat.myAgent.model.vo.StepResult;
import com.chat.myAgent.tool.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 任务规划 Agent
 *
 * 核心能力：
 * 1. 分析用户的复杂需求
 * 2. 自动拆解为多步子任务
 * 3. 按顺序（或依赖关系）逐步执行
 * 4. 汇总所有步骤结果，生成最终回答
 *
 * 工作流程：
 * 用户需求 → 规划Agent拆解 → 逐步执行(调用工具) → 汇总结果 → 最终回答
 */
@Slf4j
@Component
public class PlanningAgent {

    private final ChatClient baseChatClient;
    private final ChatClient fullAgentClient;
    private final ObjectMapper objectMapper;

    // 工具引用
    private final DateTimeTool dateTimeTool;
    private final CalculatorTool calculatorTool;
    private final TranslateTool translateTool;
    private final DocParseTool docParseTool;
    private final DbQueryTool dbQueryTool;

    @Value("classpath:prompts/planning-system.st")
    private Resource planningSystemPrompt;

    public PlanningAgent(
            @Qualifier("baseChatClient") ChatClient baseChatClient,
            @Qualifier("fullAgentClient") ChatClient fullAgentClient,
            DateTimeTool dateTimeTool,
            CalculatorTool calculatorTool,
            TranslateTool translateTool,
            DocParseTool docParseTool,
            DbQueryTool dbQueryTool) {
        this.baseChatClient = baseChatClient;
        this.fullAgentClient = fullAgentClient;
        this.objectMapper = new ObjectMapper();
        this.dateTimeTool = dateTimeTool;
        this.calculatorTool = calculatorTool;
        this.translateTool = translateTool;
        this.docParseTool = docParseTool;
        this.dbQueryTool = dbQueryTool;
    }

    /**
     * 规划并执行任务
     */
    public PlanningResponse planAndExecute(String task, String conversationId, boolean autoExecute) {
        // 处理会话 ID
        final String resolvedConversationId = resolveConversationId(conversationId);
        long startTime = System.currentTimeMillis();

        log.info("PlanningAgent [{}] 收到任务: {}", resolvedConversationId, task);

        // Step1: 让模型分析并拆解任务
        String planJson = baseChatClient.prompt()
                .system(planningSystemPrompt)
                .user(task)
                .call()
                .content();

        log.debug("规划结果 JSON: {}", planJson);

        try {
            // 清理可能的 markdown 代码块包裹
            planJson = cleanJsonResponse(planJson);
            JsonNode planNode = objectMapper.readTree(planJson);

            boolean needPlanning = planNode.path("needPlanning").asBoolean(false);

            if (!needPlanning) {
                // 简单问题，直接回答
                String directAnswer = planNode.path("directAnswer").asText("无法解析回答");
                long totalTime = System.currentTimeMillis() - startTime;

                return PlanningResponse.builder()
                        .conversationId(resolvedConversationId)
                        .planned(false)
                        .directAnswer(directAnswer)
                        .totalTimeMs(totalTime)
                        .build();
            }

            // 复杂任务，需要规划
            String taskSummary = planNode.path("taskSummary").asText("任务规划");
            JsonNode stepsNode = planNode.path("steps");

            if (!autoExecute) {
                // 只返回规划，不执行
                List<StepResult> planSteps = new ArrayList<>();
                for (JsonNode step : stepsNode) {
                    planSteps.add(StepResult.builder()
                            .stepNumber(step.path("stepNumber").asInt())
                            .description(step.path("description").asText())
                            .toolUsed(step.path("toolNeeded").asText("无"))
                            .result("未执行")
                            .success(false)
                            .build());
                }

                long totalTime = System.currentTimeMillis() - startTime;
                return PlanningResponse.builder()
                        .conversationId(resolvedConversationId)
                        .taskSummary(taskSummary)
                        .planned(true)
                        .steps(planSteps)
                        .finalAnswer("规划完成，未自动执行。共 " + planSteps.size() + " 个步骤。")
                        .totalTimeMs(totalTime)
                        .build();
            }

            // 自动执行每个步骤
            List<StepResult> executedSteps = executeSteps(stepsNode, resolvedConversationId);

            // 汇总所有结果，生成最终回答
            String finalAnswer = generateFinalAnswer(task, executedSteps);

            long totalTime = System.currentTimeMillis() - startTime;

            return PlanningResponse.builder()
                    .conversationId(resolvedConversationId)
                    .taskSummary(taskSummary)
                    .planned(true)
                    .steps(executedSteps)
                    .finalAnswer(finalAnswer)
                    .totalTimeMs(totalTime)
                    .build();

        } catch (Exception e) {
            log.error("任务规划解析失败，回退到直接执行模式", e);
            return fallbackDirectExecution(task, resolvedConversationId, startTime);
        }
    }

    /**
     * 逐步执行规划好的步骤
     */
    private List<StepResult> executeSteps(JsonNode stepsNode, String conversationId) {
        List<StepResult> results = new ArrayList<>();
        StringBuilder contextAccumulator = new StringBuilder();

        for (JsonNode step : stepsNode) {
            int stepNumber = step.path("stepNumber").asInt();
            String description = step.path("description").asText();
            String toolNeeded = step.path("toolNeeded").asText("");

            log.info("执行步骤 {}: {} (工具: {})", stepNumber, description, toolNeeded);

            long stepStart = System.currentTimeMillis();
            String stepResult;
            boolean success;

            try {
                if (toolNeeded != null && !toolNeeded.isBlank() && !toolNeeded.equals("null")) {
                    // 需要工具：通过带工具的ChatClient执行
                    String stepPrompt = buildStepPrompt(description, contextAccumulator.toString());
                    stepResult = executeWithTools(stepPrompt, toolNeeded);
                } else {
                    // 不需要工具：直接让模型处理
                    String stepPrompt = buildStepPrompt(description, contextAccumulator.toString());
                    stepResult = baseChatClient.prompt()
                            .user(stepPrompt)
                            .call()
                            .content();
                }
                success = true;
            } catch (Exception e) {
                stepResult = "步骤执行失败: " + e.getMessage();
                success = false;
                log.error("步骤 {} 执行失败", stepNumber, e);
            }

            long stepTime = System.currentTimeMillis() - stepStart;

            // 累积上下文，供后续步骤使用
            contextAccumulator.append("\n[步骤").append(stepNumber).append("结果]: ").append(stepResult);

            results.add(StepResult.builder()
                    .stepNumber(stepNumber)
                    .description(description)
                    .toolUsed(toolNeeded)
                    .result(stepResult)
                    .success(success)
                    .timeMs(stepTime)
                    .build());
        }

        return results;
    }

    /**
     * 使用指定工具执行步骤
     */
    private String executeWithTools(String prompt, String toolName) {
        Object[] tools = resolveTools(toolName);

        return baseChatClient.prompt()
                .user(prompt)
                .tools(tools)
                .call()
                .content();
    }

    /**
     * 根据工具名称获取工具实例
     */
    private Object[] resolveTools(String toolName) {
        List<Object> tools = new ArrayList<>();
        String name = toolName.toLowerCase().trim();

        if (name.contains("datetime") || name.contains("time") || name.contains("date")) {
            tools.add(dateTimeTool);
        }
        if (name.contains("calc") || name.contains("math") || name.contains("计算")) {
            tools.add(calculatorTool);
        }
        if (name.contains("translate") || name.contains("翻译")) {
            tools.add(translateTool);
        }
        if (name.contains("doc") || name.contains("file") || name.contains("文件")) {
            tools.add(docParseTool);
        }
        if (name.contains("db") || name.contains("database") || name.contains("查询")) {
            tools.add(dbQueryTool);
        }

        // 如果没匹配到，注入所有工具
        if (tools.isEmpty()) {
            tools.add(dateTimeTool);
            tools.add(calculatorTool);
            tools.add(translateTool);
            tools.add(docParseTool);
            tools.add(dbQueryTool);
        }

        return tools.toArray();
    }

    /**
     * 构建步骤执行的Prompt（包含之前步骤的结果作为上下文）
     */
    private String buildStepPrompt(String stepDescription, String previousContext) {
        StringBuilder sb = new StringBuilder();
        if (!previousContext.isBlank()) {
            sb.append("之前步骤的执行结果：\n").append(previousContext).append("\n\n");
        }
        sb.append("当前任务：").append(stepDescription);
        sb.append("\n请执行上述任务并返回结果。");
        return sb.toString();
    }

    /**
     * 汇总所有步骤结果，生成最终回答
     */
    private String generateFinalAnswer(String originalTask, List<StepResult> steps) {
        StringBuilder context = new StringBuilder();
        context.append("用户原始需求：").append(originalTask).append("\n\n");
        context.append("各步骤执行结果：\n");

        for (StepResult step : steps) {
            context.append("步骤").append(step.getStepNumber()).append(": ")
                    .append(step.getDescription()).append("\n");
            context.append("结果: ").append(step.getResult()).append("\n");
            context.append("状态: ").append(step.isSuccess() ? "✅成功" : "❌失败").append("\n\n");
        }

        context.append("请基于以上所有步骤的执行结果，生成一个完整、连贯的最终回答给用户。");

        return baseChatClient.prompt()
                .user(context.toString())
                .call()
                .content();
    }

    /**
     * 回退方案：规划解析失败时，直接使用全能Agent执行
     */
    private PlanningResponse fallbackDirectExecution(String task, String conversationId, long startTime) {
        log.info("使用回退方案直接执行: {}", task);

        String reply = fullAgentClient.prompt()
                .user(task)
                .tools(dateTimeTool, calculatorTool, translateTool, docParseTool, dbQueryTool)
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, conversationId)
                )
                .call()
                .content();

        long totalTime = System.currentTimeMillis() - startTime;

        return PlanningResponse.builder()
                .conversationId(conversationId)
                .planned(false)
                .directAnswer(reply)
                .finalAnswer(reply)
                .totalTimeMs(totalTime)
                .build();
    }

    /**
     * 清理模型返回的JSON（去除可能的markdown代码块包裹）
     */
    private String cleanJsonResponse(String json) {
        if (json == null) return "{}";
        json = json.trim();
        if (json.startsWith("```json")) {
            json = json.substring(7);
        }
        if (json.startsWith("```")) {
            json = json.substring(3);
        }
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3);
        }
        return json.trim();
    }

    private String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return "plan-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }
        return conversationId;
    }
}
