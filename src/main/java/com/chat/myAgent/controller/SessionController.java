package com.chat.myAgent.controller;

import com.chat.myAgent.agent.ChatAgent;
import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.entity.ChatHistoryEntity;
import com.chat.myAgent.model.vo.SessionSummaryVO;
import com.chat.myAgent.repository.ChatHistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
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
    private final ChatHistoryRepository chatHistoryRepository;

    /**
     * 获取指定会话的历史消息
     */
    @Operation(summary = "获取会话历史消息")
    @GetMapping("/{conversationId}/history")
    public R<List<Map<String, String>>> getHistory(@Parameter(description = "会话ID", required = true) @PathVariable String conversationId) {
        List<Message> messages = chatAgent.getHistory(conversationId);

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
     */
    @Operation(summary = "清除会话记忆", description = "清除后该conversationId的对话将从头开始")
    @DeleteMapping("/{conversationId}")
    public R<String> clearSession(@Parameter(description = "会话ID", required = true) @PathVariable String conversationId) {
        chatAgent.clearMemory(conversationId);
        chatHistoryRepository.deleteByConversationId(conversationId);
        return R.ok("会话 [" + conversationId + "] 已清除");
    }

    /**
     * 会话摘要列表
     */
    @Operation(summary = "获取会话摘要列表", description = "按 conversationId 聚合最近会话信息")
    @GetMapping("/summary")
    public R<List<SessionSummaryVO>> sessionSummary() {
        List<ChatHistoryEntity> all = chatHistoryRepository.findAll();
        Map<String, List<ChatHistoryEntity>> grouped = all.stream()
                .collect(Collectors.groupingBy(ChatHistoryEntity::getConversationId));

        List<SessionSummaryVO> summaries = grouped.entrySet().stream()
                .map(entry -> {
                    List<ChatHistoryEntity> records = entry.getValue();
                    records.sort(Comparator.comparing(ChatHistoryEntity::getCreatedAt));
                    ChatHistoryEntity last = records.get(records.size() - 1);
                    String lastMessage = last.getContent();
                    return SessionSummaryVO.builder()
                            .conversationId(entry.getKey())
                            .agentType(last.getAgentType())
                            .model(last.getModel())
                            .lastMessage(lastMessage)
                            .messageCount(records.size())
                            .lastUpdatedAt(last.getCreatedAt())
                            .build();
                })
                .sorted(Comparator.comparing(SessionSummaryVO::getLastUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());

        return R.ok(summaries);
    }
}
