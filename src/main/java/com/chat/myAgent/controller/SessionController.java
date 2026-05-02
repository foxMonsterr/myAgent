package com.chat.myAgent.controller;

import com.chat.myAgent.agent.ChatAgent;
import com.chat.myAgent.common.result.R;
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
    @GetMapping("/{conversationId}/history")
    public R<List<Map<String, String>>> getHistory(@PathVariable String conversationId) {
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
    @DeleteMapping("/{conversationId}")
    public R<String> clearSession(@PathVariable String conversationId) {
        chatAgent.clearMemory(conversationId);
        return R.ok("会话 [" + conversationId + "] 已清除");
    }
}
