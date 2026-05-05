package com.chat.myAgent.agent;

import com.chat.myAgent.model.dto.ChatRequest;
import com.chat.myAgent.model.vo.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;


@Slf4j
@Component
public class ChatAgent {

    private final ChatClient baseChatClient;
    private final ChatClient memoryChatClient;
    private final ChatMemory chatMemory;

    @Value("classpath:prompts/expert-system.st")
    private Resource expertPromptResource;

    public ChatAgent(
            @Qualifier("baseChatClient") ChatClient baseChatClient,
            @Qualifier("memoryChatClient") ChatClient memoryChatClient,
            ChatMemory chatMemory) {
        this.baseChatClient = baseChatClient;
        this.memoryChatClient = memoryChatClient;
        this.chatMemory = chatMemory;
    }

    /**
     * 基础对话（无记忆）
     */
    public ChatResponse simpleChat(ChatRequest request) {
        String conversationId = resolveConversationId(request.getConversationId());

        log.debug("SimpleChat [{}]: {}", conversationId, request.getMessage());

        String reply = baseChatClient.prompt()
                .user(request.getMessage())
                .call()
                .content();

        return ChatResponse.builder()
                .conversationId(conversationId)
                .reply(reply)
                .model("deepseek-v4-flash")
                .build();
    }

    /**
     * 多轮对话（带记忆）
     */
    public ChatResponse chat(ChatRequest request) {
        String conversationId = resolveConversationId(request.getConversationId());

        log.debug("MemoryChat [{}]: {}", conversationId, request.getMessage());

        String reply = memoryChatClient.prompt()
                .user(request.getMessage())
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, conversationId)
                )
                .call()
                .content();

        // 获取当前会话的历史消息数量
        int historySize = getHistorySize(conversationId);

        log.debug("MemoryChat [{}] 回复 (历史{}轮): {}", conversationId, historySize, reply);

        return ChatResponse.builder()
                .conversationId(conversationId)
                .reply(reply)
                .model("deepseek-v4-flash")
                .historySize(historySize)
                .build();
    }

    /**
     * 专家角色对话（动态 Prompt 模板 + 记忆）
     *
     * 演示 PromptTemplate 的动态变量替换能力：
     * - {role}: 专家领域
     * - {level}: 用户技术水平
     */
    public ChatResponse expertChat(ChatRequest request, String role, String level) {
        String conversationId = resolveConversationId(request.getConversationId());

        log.debug("ExpertChat [{}] role={}, level={}: {}",
                conversationId, role, level, request.getMessage());

        String reply = memoryChatClient.prompt()
                .system(s -> s
                        .text(expertPromptResource)
                        .param("role", role)
                        .param("level", level)
                )
                .user(request.getMessage())
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, conversationId)
                )
                .call()
                .content();

        int historySize = getHistorySize(conversationId);

        return ChatResponse.builder()
                .conversationId(conversationId)
                .reply(reply)
                .model("deepseek-v4-flash")
                .historySize(historySize)
                .build();
    }

    /**
     * 获取指定会话的历史消息
     */
    public List<Message> getHistory(String conversationId) {
        return chatMemory.get(conversationId);
    }

    /**
     * 清除指定会话的记忆
     */
    public void clearMemory(String conversationId) {
        chatMemory.clear(conversationId);
        log.info("已清除会话记忆: {}", conversationId);
    }

    /**
     * 获取会话历史消息数量
     */
    private int getHistorySize(String conversationId) {
        List<Message> messages = chatMemory.get(conversationId);
        return messages != null ? messages.size() : 0;
    }

    /**
     * 解析会话ID，为空则自动生成
     */
    private String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        return conversationId;
    }
}
