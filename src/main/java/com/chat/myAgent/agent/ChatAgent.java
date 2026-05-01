package com.chat.myAgent.agent;


import com.chat.myAgent.model.dto.ChatRequest;
import com.chat.myAgent.model.vo.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 基础对话 Agent
 * 
 * 阶段1: 简单的单轮对话
 * 阶段2: 将升级为多轮对话（加入 ChatMemory）
 * 
 * 设计思路：
 * - Agent 层负责"编排"，决定如何使用底层能力
 * - Controller 层只做参数接收和格式转换
 * - Agent 可以被其他 Agent 组合调用（为 PlanningAgent 预留）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatAgent {

    //  指定要注入名为 "baseChatClient" 的 Bean
    @Qualifier("baseChatClient")
    private final ChatClient chatClient;

    /**
     * 基础对话
     * 阶段1: 单轮对话，不带记忆
     */
    public ChatResponse chat(ChatRequest request) {
        String conversationId = request.getConversationId();
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString().replace("-", "");
        }

        log.debug("ChatAgent 收到消息 [conversationId={}]: {}", conversationId, request.getMessage());

        // 调用大模型
        String reply = chatClient.prompt()
                .user(request.getMessage())
                .call()
                .content();

        log.debug("ChatAgent 回复 [conversationId={}]: {}", conversationId, reply);

        // 组装响应
        return ChatResponse.builder()
                .conversationId(conversationId)
                .reply(reply)
                .model("deepseek-v4-flash")  // 阶段1暂用默认，后续从响应元数据中提取
                .build();
    }

    /**
     * 流式对话（扩展预留，阶段2/3实现）
     * 返回类型将改为 Flux<String>，通过 SSE 推送到前端
     */
    // public Flux<String> chatStream(ChatRequest request) { ... }
}
