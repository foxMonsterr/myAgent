package com.chat.myAgent.controller;

import com.chat.myAgent.agent.ChatAgent;
import com.chat.myAgent.agent.StructuredAgent;
import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.dto.ChatRequest;
import com.chat.myAgent.model.dto.StructuredRequest;
import com.chat.myAgent.model.entity.BookInfo;
import com.chat.myAgent.model.entity.SentimentResult;
import com.chat.myAgent.model.entity.TaskAnalysis;
import com.chat.myAgent.model.vo.ChatResponse;
import com.chat.myAgent.model.vo.StructuredResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 对话接口（阶段2升级版）
 *
 * 新增接口：
 * - POST /api/v1/chat/memory       多轮记忆对话
 * - POST /api/v1/chat/expert       专家角色对话
 * - POST /api/v1/chat/structured/* 结构化输出系列接口
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatAgent chatAgent;
    private final StructuredAgent structuredAgent;

    // ==================== 基础对话（阶段1保留） ====================

    /**
     * 简单对话（无记忆）
     * POST /api/v1/chat/simple
     */
    @PostMapping("/simple")
    public R<ChatResponse> simpleChat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatAgent.simpleChat(request);
        return R.ok(response);
    }

    /**
     * 健康检查
     * GET /api/v1/chat/ping
     */
    @GetMapping("/ping")
    public R<String> ping() {
        return R.ok("SmartAgent is running! 🚀 (Phase 2: Memory + Prompt + Structured Output)");
    }

    /**
     * 快速对话（GET方式，浏览器测试用）
     * GET /api/v1/chat/quick?message=xxx
     */
    @GetMapping("/quick")
    public R<ChatResponse> quickChat(@RequestParam String message) {
        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        return R.ok(chatAgent.simpleChat(request));
    }

    // ==================== 多轮记忆对话（阶段2核心） ====================

    /**
     * 多轮记忆对话
     * POST /api/v1/chat/memory
     *
     * 特性：
     * - 相同 conversationId 的对话会记住之前的内容
     * - 不同 conversationId 互不干扰
     * - 不传 conversationId 则自动生成新会话
     *
     * 测试步骤：
     * 1. 发送 {"conversationId":"test1", "message":"我叫小明"}
     * 2. 发送 {"conversationId":"test1", "message":"我叫什么？"} → AI应能回答"小明"
     * 3. 发送 {"conversationId":"test2", "message":"我叫什么？"} → AI不知道（不同会话）
     */
    @PostMapping("/memory")
    public R<ChatResponse> memoryChat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatAgent.chat(request);
        return R.ok(response);
    }

    // ==================== 专家角色对话（Prompt模板演示） ====================

    /**
     * 专家角色对话
     * POST /api/v1/chat/expert
     *
     * 演示 PromptTemplate 动态变量替换：
     * - role: 专家领域（如 Java、Python、前端）
     * - level: 用户水平（如 beginner、intermediate、advanced）
     *
     * 请求示例：
     * {
     *   "conversationId": "expert-java-001",
     *   "message": "什么是 Spring IoC？",
     *   "role": "Java",
     *   "level": "beginner"
     * }
     */
    @PostMapping("/expert")
    public R<ChatResponse> expertChat(@Valid @RequestBody ChatRequest request) {
        String role = request.getRole() != null ? request.getRole() : "通用技术";
        String level = request.getLevel() != null ? request.getLevel() : "intermediate";

        ChatResponse response = chatAgent.expertChat(request, role, level);
        return R.ok(response);
    }

    // ==================== 结构化输出（阶段2新增） ====================

    /**
     * 结构化输出 - 图书信息提取
     * POST /api/v1/chat/structured/book
     *
     * 请求示例：
     * {"input": "推荐一本适合Java初学者的编程书"}
     *
     * 响应：固定格式的 BookInfo JSON
     */
    @PostMapping("/structured/book")
    public R<StructuredResponse<BookInfo>> structuredBook(@Valid @RequestBody StructuredRequest request) {
        BookInfo bookInfo = structuredAgent.extractBookInfo(request.getInput());
        StructuredResponse<BookInfo> response = StructuredResponse.<BookInfo>builder()
                .result(bookInfo)
                .outputType("book")
                .originalInput(request.getInput())
                .build();
        return R.ok(response);
    }

    /**
     * 结构化输出 - 任务分析
     * POST /api/v1/chat/structured/task
     *
     * 请求示例：
     * {"input": "帮我读取文档并翻译成英文，然后计算字数"}
     *
     * 响应：结构化的任务分析结果（类型、复杂度、步骤、所需工具）
     */
    @PostMapping("/structured/task")
    public R<StructuredResponse<TaskAnalysis>> structuredTask(@Valid @RequestBody StructuredRequest request) {
        TaskAnalysis taskAnalysis = structuredAgent.analyzeTask(request.getInput());
        StructuredResponse<TaskAnalysis> response = StructuredResponse.<TaskAnalysis>builder()
                .result(taskAnalysis)
                .outputType("task")
                .originalInput(request.getInput())
                .build();
        return R.ok(response);
    }

    /**
     * 结构化输出 - 情感分析
     * POST /api/v1/chat/structured/sentiment
     *
     * 请求示例：
     * {"input": "今天天气真好，心情特别愉快！"}
     */
    @PostMapping("/structured/sentiment")
    public R<StructuredResponse<SentimentResult>> structuredSentiment(@Valid @RequestBody StructuredRequest request) {
        SentimentResult sentimentResult = structuredAgent.analyzeSentiment(request.getInput());
        StructuredResponse<SentimentResult> response = StructuredResponse.<SentimentResult>builder()
                .result(sentimentResult)
                .outputType("sentiment")
                .originalInput(request.getInput())
                .build();
        return R.ok(response);
    }
}
