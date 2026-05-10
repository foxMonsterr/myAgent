package com.chat.myAgent.agent;

import com.chat.myAgent.common.context.TraceContext;
import com.chat.myAgent.config.ModelConfig;
import com.chat.myAgent.model.dto.ChatRequest;
import com.chat.myAgent.model.vo.ChatResponse;
import com.chat.myAgent.service.AuditService;
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
 * 基础聊天 Agent
 *
 * 说明：
 * - 默认优先使用 deepseek-v4-flash
 * - 当主模型请求失败时，自动切换到 qwen3.6-plus 兜底
 * - 这里的兜底逻辑只负责“请求失败后的再次尝试”，不会影响正常主链路
 */
@Slf4j
@Component
public class ChatAgent {

    private final ChatClient baseChatClient;
    private final ChatClient memoryChatClient;
    private final ChatClient fallbackChatClient;
    private final ChatMemory chatMemory;
    private final ModelConfig modelConfig;
    private final AuditService auditService;

    @Value("classpath:prompts/expert-system.st")
    private Resource expertPromptResource;

    public ChatAgent(
            @Qualifier("baseChatClient") ChatClient baseChatClient,
            @Qualifier("memoryChatClient") ChatClient memoryChatClient,
            @Qualifier("fallbackChatClient") ChatClient fallbackChatClient,
            ChatMemory chatMemory,
            ModelConfig modelConfig,
            AuditService auditService) {
        this.baseChatClient = baseChatClient;
        this.memoryChatClient = memoryChatClient;
        this.fallbackChatClient = fallbackChatClient;
        this.chatMemory = chatMemory;
        this.modelConfig = modelConfig;
        this.auditService = auditService;
    }

    /**
     * 基础对话（无记忆）
     */
    public ChatResponse simpleChat(ChatRequest request) {
        return simpleChat(request, false);
    }

    /**
     * 基础对话（无记忆），支持思考模式
     */
    public ChatResponse simpleChat(ChatRequest request, boolean thinkingMode) {
        String conversationId = resolveConversationId(request.getConversationId());
        String userMessage = request.getMessage();

        log.debug("SimpleChat [{}] thinkingMode={}: {}", conversationId, thinkingMode, userMessage);

        try {
            String reply = baseChatClient.prompt()
                    .system(buildThinkingSystemPrompt(thinkingMode, false, null, null))
                    .user(userMessage)
                    .call()
                    .content();

            ChatResponse response = buildResponse(conversationId, reply, modelConfig.getPrimaryModel(), null, thinkingMode);
            auditService.saveChatHistory(conversationId, "anonymous", "user", userMessage, "simple", modelConfig.getPrimaryModel(), null, null, 0L);
            auditService.saveChatHistory(conversationId, "anonymous", "assistant", response.getReply(), "simple", modelConfig.getPrimaryModel(), null, null, 0L);
            auditService.saveAgentInvocation(conversationId, "simple", modelConfig.getPrimaryModel(), userMessage, response.getReply(), response.getThinking(), "SUCCESS", 0L);
            return response;
        } catch (Exception primaryEx) {
            log.warn("SimpleChat 主模型调用失败，准备切换兜底模型: {}", primaryEx.getMessage());
            return simpleChatFallback(conversationId, userMessage, thinkingMode, primaryEx);
        }
    }

    /**
     * 多轮对话（带记忆）
     */
    public ChatResponse chat(ChatRequest request) {
        return chat(request, false);
    }

    /**
     * 多轮对话（带记忆），支持思考模式
     */
    public ChatResponse chat(ChatRequest request, boolean thinkingMode) {
        String conversationId = resolveConversationId(request.getConversationId());
        String userMessage = request.getMessage();

        log.debug("MemoryChat [{}] thinkingMode={}: {}", conversationId, thinkingMode, userMessage);

        try {
            String reply = memoryChatClient.prompt()
                    .system(buildThinkingSystemPrompt(thinkingMode, true, null, null))
                    .user(userMessage)
                    .advisors(advisor -> advisor
                            .param(ChatMemory.CONVERSATION_ID, conversationId)
                    )
                    .call()
                    .content();

            int historySize = getHistorySize(conversationId);
            ChatResponse response = buildResponse(conversationId, reply, modelConfig.getPrimaryModel(), historySize, thinkingMode);
            auditService.saveChatHistory(conversationId, "anonymous", "user", userMessage, "memory", modelConfig.getPrimaryModel(), null, null, 0L);
            auditService.saveChatHistory(conversationId, "anonymous", "assistant", response.getReply(), "memory", modelConfig.getPrimaryModel(), null, null, 0L);
            auditService.saveAgentInvocation(conversationId, "memory", modelConfig.getPrimaryModel(), userMessage, response.getReply(), response.getThinking(), "SUCCESS", 0L);

            log.debug("MemoryChat [{}] 回复 (历史{}轮): {}", conversationId, historySize, response.getReply());
            return response;
        } catch (Exception primaryEx) {
            log.warn("MemoryChat 主模型调用失败，准备切换兜底模型: {}", primaryEx.getMessage());
            return memoryChatFallback(conversationId, userMessage, thinkingMode, primaryEx);
        }
    }

    /**
     * 专家角色对话（动态 Prompt 模板 + 记忆）
     */
    public ChatResponse expertChat(ChatRequest request, String role, String level) {
        return expertChat(request, role, level, false);
    }

    /**
     * 专家角色对话（动态 Prompt 模板 + 记忆），支持思考模式
     */
    public ChatResponse expertChat(ChatRequest request, String role, String level, boolean thinkingMode) {
        String conversationId = resolveConversationId(request.getConversationId());
        String userMessage = request.getMessage();

        log.debug("ExpertChat [{}] role={}, level={}, thinkingMode={}: {}",
                conversationId, role, level, thinkingMode, userMessage);

        try {
            String reply = baseChatClient.prompt()
                    .system(s -> s
                            .text(expertPromptResource)
                            .param("role", role)
                            .param("level", level)
                    )
                    .system(buildThinkingSystemPrompt(thinkingMode, true, role, level))
                    .user(userMessage)
                    .advisors(advisor -> advisor
                            .param(ChatMemory.CONVERSATION_ID, conversationId)
                    )
                    .call()
                    .content();

            int historySize = getHistorySize(conversationId);
            ChatResponse response = buildResponse(conversationId, reply, modelConfig.getPrimaryModel(), historySize, thinkingMode);
            auditService.saveChatHistory(conversationId, "anonymous", "user", userMessage, "expert", modelConfig.getPrimaryModel(), null, null, 0L);
            auditService.saveChatHistory(conversationId, "anonymous", "assistant", response.getReply(), "expert", modelConfig.getPrimaryModel(), null, null, 0L);
            auditService.saveAgentInvocation(conversationId, "expert", modelConfig.getPrimaryModel(), userMessage, response.getReply(), response.getThinking(), "SUCCESS", 0L);
            return response;
        } catch (Exception primaryEx) {
            log.warn("ExpertChat 主模型调用失败，准备切换兜底模型: {}", primaryEx.getMessage());
            return expertChatFallback(conversationId, userMessage, role, level, thinkingMode, primaryEx);
        }
    }

    /**
     * 构造思考模式系统提示
     */
    private String buildThinkingSystemPrompt(boolean thinkingMode, boolean useMemory, String role, String level) {
        if (!thinkingMode) {
            return "请直接、简洁、准确地回答用户，不要输出思考过程。";
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append("你需要先进行思考，再给出最终答案。\n");
        prompt.append("如果模型支持思考模式，请先在内部推理，再输出最终回答。\n");
        prompt.append("如果无法显式输出思考过程，也请只返回最终答案。\n");
        if (useMemory) {
            prompt.append("当前场景需要结合上下文进行回答。\n");
        }
        if (role != null && !role.isBlank()) {
            prompt.append("角色：").append(role).append("。\n");
        }
        if (level != null && !level.isBlank()) {
            prompt.append("等级：").append(level).append("。\n");
        }
        return prompt.toString();
    }

    private ChatResponse buildResponse(String conversationId, String reply, String modelName, Integer historySize, boolean thinkingMode) {
        ThinkingParts parts = parseThinkingReply(reply, thinkingMode);
        return ChatResponse.builder()
                .conversationId(conversationId)
                .reply(parts.finalAnswer())
                .thinking(parts.thinking())
                .traceId(TraceContext.getTraceId())
                .model(modelName)
                .historySize(historySize)
                .build();
    }

    /**
     * 主模型失败后的基础对话兜底
     */
    private ChatResponse simpleChatFallback(String conversationId, String userMessage, boolean thinkingMode, Exception primaryEx) {
        try {
            String reply = fallbackChatClient.prompt()
                    .system(buildThinkingSystemPrompt(thinkingMode, false, null, null))
                    .user(userMessage)
                    .call()
                    .content();

            ChatResponse response = buildResponse(conversationId, reply, modelConfig.getFallbackModelName(), null, thinkingMode);
            auditService.saveAgentInvocation(conversationId, "simple-fallback", modelConfig.getFallbackModelName(), userMessage, response.getReply(), response.getThinking(), "SUCCESS", 0L);
            return response;
        } catch (Exception fallbackEx) {
            log.error("SimpleChat 兜底模型也调用失败", fallbackEx);
            throw new RuntimeException("主模型与兜底模型均调用失败: " + fallbackEx.getMessage(), fallbackEx);
        }
    }

    /**
     * 主模型失败后的记忆对话兜底
     */
    private ChatResponse memoryChatFallback(String conversationId, String userMessage, boolean thinkingMode, Exception primaryEx) {
        try {
            String reply = fallbackChatClient.prompt()
                    .system(buildThinkingSystemPrompt(thinkingMode, true, null, null))
                    .user(userMessage)
                    .advisors(advisor -> advisor
                            .param(ChatMemory.CONVERSATION_ID, conversationId)
                    )
                    .call()
                    .content();

            int historySize = getHistorySize(conversationId);
            ChatResponse response = buildResponse(conversationId, reply, modelConfig.getFallbackModelName(), historySize, thinkingMode);
            auditService.saveAgentInvocation(conversationId, "memory-fallback", modelConfig.getFallbackModelName(), userMessage, response.getReply(), response.getThinking(), "SUCCESS", 0L);
            return response;
        } catch (Exception fallbackEx) {
            log.error("MemoryChat 兜底模型也调用失败", fallbackEx);
            throw new RuntimeException("主模型与兜底模型均调用失败: " + fallbackEx.getMessage(), fallbackEx);
        }
    }

    /**
     * 主模型失败后的专家模式兜底
     */
    private ChatResponse expertChatFallback(String conversationId, String userMessage, String role, String level, boolean thinkingMode, Exception primaryEx) {
        try {
            String reply = fallbackChatClient.prompt()
                    .system(s -> s
                            .text(expertPromptResource)
                            .param("role", role)
                            .param("level", level)
                    )
                    .system(buildThinkingSystemPrompt(thinkingMode, true, role, level))
                    .user(userMessage)
                    .advisors(advisor -> advisor
                            .param(ChatMemory.CONVERSATION_ID, conversationId)
                    )
                    .call()
                    .content();

            int historySize = getHistorySize(conversationId);
            ChatResponse response = buildResponse(conversationId, reply, modelConfig.getFallbackModelName(), historySize, thinkingMode);
            auditService.saveAgentInvocation(conversationId, "expert-fallback", modelConfig.getFallbackModelName(), userMessage, response.getReply(), response.getThinking(), "SUCCESS", 0L);
            return response;
        } catch (Exception fallbackEx) {
            log.error("ExpertChat 兜底模型也调用失败", fallbackEx);
            throw new RuntimeException("主模型与兜底模型均调用失败: " + fallbackEx.getMessage(), fallbackEx);
        }
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

    private ThinkingParts parseThinkingReply(String reply, boolean thinkingMode) {
        if (reply == null || reply.isBlank()) {
            return new ThinkingParts(reply, null);
        }
        if (!thinkingMode) {
            return new ThinkingParts(reply.trim(), null);
        }
        String trimmed = reply.trim();
        int finalAnswerIndex = indexOfAny(trimmed, "最终回答：", "Final Answer:", "Answer:");
        if (finalAnswerIndex >= 0) {
            String thinking = trimmed.substring(0, finalAnswerIndex).trim();
            String answer = trimmed.substring(finalAnswerIndex).trim();
            return new ThinkingParts(stripAnswerLabel(answer), thinking.isBlank() ? null : thinking);
        }
        return new ThinkingParts(trimmed, null);
    }

    private int indexOfAny(String text, String... markers) {
        int index = -1;
        for (String marker : markers) {
            int current = text.indexOf(marker);
            if (current >= 0 && (index < 0 || current < index)) {
                index = current;
            }
        }
        return index;
    }

    private String stripAnswerLabel(String text) {
        return text.replaceFirst("^(最终回答：|Final Answer:|Answer:)\\s*", "").trim();
    }

    private String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        return conversationId;
    }

    private record ThinkingParts(String finalAnswer, String thinking) {}
}
