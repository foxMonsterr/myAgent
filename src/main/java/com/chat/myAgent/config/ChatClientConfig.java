package com.chat.myAgent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatClient 核心配置
 * 
 * 设计说明：
 * - 这里创建一个"基础版"ChatClient，仅具备基本对话能力
 * - 后续阶段会创建带 Memory、Tools、RAG 的增强版 ChatClient
 * - 采用 Builder 模式，便于灵活组合不同能力
 */
@Configuration
public class ChatClientConfig {

    /**
     * 基础 ChatClient
     * 仅用于阶段1的简单对话，后续阶段会注入更多 Advisor
     */
    @Bean("baseChatClient")  // ← 创建一个名为 "baseChatClient" 的 Bean
    public ChatClient baseChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("你是一个智能助手，名叫 SmartAgent。" +
                        "你的回答简洁、准确、有帮助。" +
                        "如果不确定答案，请诚实告知。")
                .build();
    }

    /**
     * 扩展预留：后续阶段会在这里添加更多 ChatClient Bean
     * 
     * @Bean("memoryChatClient")   → 阶段2: 带多轮记忆
     * @Bean("toolChatClient")     → 阶段3: 带工具调用
     * @Bean("ragChatClient")      → 阶段4: 带 RAG 检索
     * @Bean("agentChatClient")    → 阶段5: 全能力 Agent
     */
}
