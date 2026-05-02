package com.chat.myAgent.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 聊天记忆配置
 *
 * 设计说明：
 * - 阶段2使用 InMemoryChatMemory（内存存储，重启丢失）
 * - 阶段5升级为 Redis 持久化存储（RedisChatMemory）
 * - ChatMemory 接口是 Spring AI 的标准抽象，切换实现无需修改业务代码
 *
 * InMemoryChatMemory 特性：
 * - 基于 ConcurrentHashMap，线程安全
 * - 按 conversationId 隔离不同会话
 * - 应用重启后记忆清空（开发阶段够用）
 */
@Configuration
public class MemoryConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(100)  // 保留最近100条消息
                .buld();
    }
}
