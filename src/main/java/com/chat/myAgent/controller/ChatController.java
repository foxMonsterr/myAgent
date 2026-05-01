package com.chat.myAgent.controller;


import com.chat.myAgent.agent.ChatAgent;
import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.dto.ChatRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.chat.myAgent.model.vo.ChatResponse;
import org.springframework.web.bind.annotation.*;

/**
 * 基础对话接口
 * 
 * 路径规范：/api/v1/chat/**
 * - v1 版本号预留，便于后续接口升级时不影响老接口
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatAgent chatAgent;

    /**
     * 简单对话接口
     * POST /api/v1/chat
     * 
     * 请求体:
     * {
     *   "message": "你好",
     *   "conversationId": "可选，不传则自动生成"
     * }
     */
    @PostMapping
    public R<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatAgent.chat(request);
        return R.ok(response);
    }

    /**
     * 健康检查 / 快速测试接口
     * GET /api/v1/chat/ping
     * 
     * 用于验证服务是否启动、配置是否正确
     */
    @GetMapping("/ping")
    public R<String> ping() {
        return R.ok("SmartAgent is running! 🚀");
    }

    /**
     * 快速对话（GET方式，便于浏览器直接测试）
     * GET /api/v1/chat/quick?message=你好
     */
    @GetMapping("/quick")
    public R<ChatResponse> quickChat(@RequestParam String message) {
        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        return R.ok(chatAgent.chat(request));
    }
}
