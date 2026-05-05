package com.chat.myAgent.controller;

import com.chat.myAgent.agent.ChatAgent;
import com.chat.myAgent.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 会话管理接口
 *
 * 用于管理对话会话：查看历史、清除记忆等
 */
@Tag(name = "会话管理", description = "查看会话历史、清除会话记忆")
@RestController
@RequestMapping("/api/v1/session")
@RequiredArgsConstructor
public class SessionController {

    private final ChatAgent chatAgent;

    /**
     * 获取指定会话的历史消息
     * GET /api/v1/session/{conversationId}/history
     *
     * 返回该会话的所有历史消息列表
     */
    @Operation(summary = "获取会话历史消息")
    @GetMapping("/{conversationId}/history")
    public R<List<Map<String, String>>> getHistory(@Parameter(description = "会话ID", required = true) @PathVariable String conversationId) {
        List<Message> messages = chatAgent.getHistory(conversationId);

        // 转换为前端友好的格式
        List<Map<String, String>> history = messages.stream()
                .map(msg -> {
                    Map<String, String> item = new HashMap<>();
                    item.put("role", msg.getMessageType().name().toLowerCase());
                    item.put("content", msg.getText());
                    return item;
                })
                .collect(Collectors.toList());

        return R.ok(history);
    }

    /**
     * 清除指定会话的记忆
     * DELETE /api/v1/session/{conversationId}
     *
     * 清除后，该 conversationId 的对话将从头开始
     */
    @Operation(summary = "清除会话记忆", description = "清除后该conversationId的对话将从头开始")
    @DeleteMapping("/{conversationId}")
    public R<String> clearSession(@Parameter(description = "会话ID", required = true) @PathVariable String conversationId) {
        chatAgent.clearMemory(conversationId);
        return R.ok("会话 [" + conversationId + "] 已清除");
    }
}
