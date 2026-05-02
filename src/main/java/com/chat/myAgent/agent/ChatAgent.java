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

/**
 * 基础对话 Agent（阶段2升级版）
 *
 * 新增能力：
 * 1. 多轮对话记忆（自动加载/保存历史消息）
 * 2. 会话隔离（不同 conversationId 独立记忆）
 * 3. 动态角色切换（通过 Prompt 模板实现）
 * 4. 会话管理（查看历史、清除记忆）
 */
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
     * 基础对话（无记忆，保持阶段1兼容）
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
     * 多轮对话（带记忆，阶段2核心功能）
     *
     * 工作流程：
     * 1. MessageChatMemoryAdvisor 根据 conversationId 加载历史消息
     * 2. 将历史消息 + 当前消息一起发送给大模型
     * 3. 收到响应后，自动将本轮对话（用户消息+AI回复）保存到 Memory
     * 4. 下次同一 conversationId 请求时，自动携带之前的历史
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
