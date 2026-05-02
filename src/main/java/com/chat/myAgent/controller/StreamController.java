package com.chat.myAgent.controller;

import com.chat.myAgent.agent.StreamAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 流式对话接口
 *
 * 使用 SSE（Server-Sent Events）实时推送AI回复
 *
 * 前端通过 EventSource 或 fetch + ReadableStream 接收
 */
@RestController
@RequestMapping("/api/v1/stream")
@RequiredArgsConstructor
public class StreamController {

    private final StreamAgent streamAgent;

    /**
     * 流式对话（基础版）
     * GET /api/v1/stream/chat?message=xxx&conversationId=xxx
     *
     * 返回 text/event-stream，浏览器可直接通过 EventSource 接收
     *
     * 浏览器测试：直接在地址栏输入
     * http://localhost:8080/api/v1/stream/chat?message=用200字介绍一下Spring框架
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(
            @RequestParam String message,
            @RequestParam(required = false) String conversationId) {
        return streamAgent.streamChat(message, conversationId);
    }

    /**
     * 流式对话（带工具）
     * GET /api/v1/stream/chat/tools?message=xxx
     */
    @GetMapping(value = "/chat/tools", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChatWithTools(
            @RequestParam String message,
            @RequestParam(required = false) String conversationId) {
        return streamAgent.streamChatWithTools(message, conversationId);
    }
}
