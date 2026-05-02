package com.chat.myAgent.agent;

import com.chat.myAgent.tool.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * 流式对话 Agent
 *
 * 核心能力：通过 SSE（Server-Sent Events）实时推送AI生成的内容
 * 
 * 与普通对话的区别：
 * - 普通对话：等AI全部生成完毕后一次性返回 → 用户等待时间长
 * - 流式对话：AI边生成边推送 → 用户立即看到内容，体验更好
 *
 * Spring AI 通过 .stream() 方法返回 Flux<String>，天然支持流式
 */
@Slf4j
@Component
public class StreamAgent {

    private final ChatClient fullAgentClient;
    private final ChatClient baseChatClient;

    private final DateTimeTool dateTimeTool;
    private final CalculatorTool calculatorTool;
    private final TranslateTool translateTool;
    private final DocParseTool docParseTool;
    private final DbQueryTool dbQueryTool;

    @Value("classpath:prompts/full-agent-system.st")
    private Resource fullAgentPrompt;

    public StreamAgent(
            @Qualifier("fullAgentClient") ChatClient fullAgentClient,
            @Qualifier("baseChatClient") ChatClient baseChatClient,
            DateTimeTool dateTimeTool,
            CalculatorTool calculatorTool,
            TranslateTool translateTool,
            DocParseTool docParseTool,
            DbQueryTool dbQueryTool) {
        this.fullAgentClient = fullAgentClient;
        this.baseChatClient = baseChatClient;
        this.dateTimeTool = dateTimeTool;
        this.calculatorTool = calculatorTool;
        this.translateTool = translateTool;
        this.docParseTool = docParseTool;
        this.dbQueryTool = dbQueryTool;
    }

    /**
     * 基础流式对话（无工具）
     */
    public Flux<String> streamChat(String message, String conversationId) {
        final String resolvedId = resolveConversationId(conversationId);
        log.debug("StreamAgent [{}] 流式对话: {}", resolvedId, message);

        return fullAgentClient.prompt()
                .user(message)
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, resolvedId)
                )
                .stream()
                .content();
    }

    /**
     * 带工具的流式对话
     *
     * 注意：部分模型的Function Calling在流式模式下行为可能不同
     * 工具调用阶段不会产生流式输出，只有最终回答才会流式返回
     */
    public Flux<String> streamChatWithTools(String message, String conversationId) {
        final String resolvedId = resolveConversationId(conversationId);
        log.debug("StreamAgent(Tools) [{}] 流式对话: {}", resolvedId, message);

        return baseChatClient.prompt()
                .system(fullAgentPrompt)
                .user(message)
                .tools(dateTimeTool, calculatorTool, translateTool, docParseTool, dbQueryTool)
                .stream()
                .content();
    }

    private String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return "stream-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }
        return conversationId;
    }
}
