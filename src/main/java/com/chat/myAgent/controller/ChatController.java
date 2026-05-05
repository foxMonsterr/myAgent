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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@Tag(name = "对话管理", description = "简单对话、记忆对话、专家角色、结构化输出")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatAgent chatAgent;
    private final StructuredAgent structuredAgent;


    /**
     * 简单对话（无记忆）
     * POST /api/v1/chat/simple
     */
    @Operation(summary = "简单对话（无记忆）")
    @PostMapping("/simple")
    public R<ChatResponse> simpleChat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatAgent.simpleChat(request);
        return R.ok(response);
    }

    /**
     * 健康检查
     * GET /api/v1/chat/ping
     */
    @Operation(summary = "健康检查")
    @GetMapping("/ping")
    public R<String> ping() {
        return R.ok("SmartAgent is running! 🚀 (Phase 2: Memory + Prompt + Structured Output)");
    }

    /**
     * 快速对话（GET方式，浏览器测试用）
     * GET /api/v1/chat/quick?message=xxx
     */
    @Operation(summary = "快速对话")
    @GetMapping("/quick")
    public R<ChatResponse> quickChat(@Parameter(description = "对话消息", required = true) @RequestParam String message) {
        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        return R.ok(chatAgent.simpleChat(request));
    }

 
    @Operation(summary = "多轮记忆对话", description = "相同conversationId的对话会记住之前的内容，不同conversationId互不干扰")
    @PostMapping("/memory")
    public R<ChatResponse> memoryChat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatAgent.chat(request);
        return R.ok(response);
    }

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
    @Operation(summary = "专家角色对话", description = "PromptTemplate动态变量替换，可指定专家领域和用户水平")
    @PostMapping("/expert")
    public R<ChatResponse> expertChat(@Valid @RequestBody ChatRequest request) {
        String role = request.getRole() != null ? request.getRole() : "通用技术";
        String level = request.getLevel() != null ? request.getLevel() : "intermediate";

        ChatResponse response = chatAgent.expertChat(request, role, level);
        return R.ok(response);
    }

    /**
     * 结构化输出 - 图书信息提取
     * POST /api/v1/chat/structured/book
     */
    @Operation(summary = "结构化输出 - 图书信息提取")
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
     */
    @Operation(summary = "结构化输出 - 任务分析", description = "返回任务类型、复杂度、步骤、所需工具")
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
    @Operation(summary = "结构化输出 - 情感分析")
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
