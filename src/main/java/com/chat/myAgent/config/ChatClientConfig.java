package com.chat.myAgent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;


/**
 * ChatClient 核心配置
 *
 * 设计说明：
 * - baseChatClient：无记忆的简单对话（保留阶段1功能）
 * - memoryChatClient：带多轮记忆的对话客户端（阶段2核心）
 * - 后续阶段继续扩展 toolChatClient / ragChatClient
 */
@Configuration
public class ChatClientConfig {

    @Value("classpath:prompts/chat-system.st")
    private Resource chatSystemPrompt;

    @Value("classpath:prompts/rag-system.st")
    private Resource ragSystemPrompt;

    /**
     * 基础 ChatClient（无记忆，保持阶段1兼容）
     */
    @Bean("baseChatClient")
    public ChatClient baseChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("你是一个智能助手，名叫 SmartAgent。你的回答简洁、准确、有帮助。")
                .build();
    }

    /**
     * 带记忆的 ChatClient（阶段2核心）
     *
     * 设计要点：
     * - MessageChatMemoryAdvisor 自动在每次请求时加载历史消息，响应后保存新消息
     * - conversationId 在调用时动态传入，实现会话隔离
     * - 系统提示词从外部 .st 文件加载，解耦代码与提示词
     */
    @Bean("memoryChatClient")
    public ChatClient memoryChatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultSystem(chatSystemPrompt)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .build()
                )
                .build();
    }

    /**
     * 带记忆的工具调用 ChatClient
     * 如果 ToolAgent 的 chatWithMemory 方法不生效，使用这个 Bean
     */
    @Bean("toolChatClient")
    public ChatClient toolChatClient(ChatClient.Builder builder, ChatMemory chatMemory,
                                     @Value("classpath:prompts/tool-agent-system.st") Resource toolPrompt) {
        return builder
                .defaultSystem(toolPrompt)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    /**
     * RAG 知识库问答 ChatClient
     *
     * 设计要点：
     * - 使用 QuestionAnswerAdvisor 自动完成 "检索→注入Prompt→回答" 全流程
     * - 结合 Memory 实现知识库的多轮追问
     */
    @Bean("ragChatClient")
    public ChatClient ragChatClient(ChatClient.Builder builder,
                                    ChatMemory chatMemory,
                                    VectorStore vectorStore) {
        return builder
                .defaultSystem(ragSystemPrompt)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(
                                        SearchRequest.builder()
                                                .topK(5)
                                                .similarityThreshold(0.4)
                                                .build()
                                )
                                .build()
                )
                .build();
    }


    /**
     * 扩展预留：
     * @Bean("agentChatClient")  → 阶段5: 全能力组合
     */

}
