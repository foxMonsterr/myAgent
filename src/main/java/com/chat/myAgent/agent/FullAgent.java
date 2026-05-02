package com.chat.myAgent.agent;

import com.chat.myAgent.model.vo.AgentResponse;
import com.chat.myAgent.tool.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 全能力 Agent（统一入口）
 *
 * 整合所有能力：记忆 + 工具调用 + 系统提示
 * 作为对外暴露的统一智能助手入口
 */
@Slf4j
@Component
public class FullAgent {

    private final ChatClient fullAgentClient;

    private final DateTimeTool dateTimeTool;
    private final CalculatorTool calculatorTool;
    private final TranslateTool translateTool;
    private final DocParseTool docParseTool;
    private final DbQueryTool dbQueryTool;

    public FullAgent(
            @Qualifier("fullAgentClient") ChatClient fullAgentClient,
            DateTimeTool dateTimeTool,
            CalculatorTool calculatorTool,
            TranslateTool translateTool,
            DocParseTool docParseTool,
            DbQueryTool dbQueryTool) {
        this.fullAgentClient = fullAgentClient;
        this.dateTimeTool = dateTimeTool;
        this.calculatorTool = calculatorTool;
        this.translateTool = translateTool;
        this.docParseTool = docParseTool;
        this.dbQueryTool = dbQueryTool;
    }

    /**
     * 全能力对话
     *
     * 具备：多轮记忆 + 工具自主调用 + 系统角色设定
     */
    public AgentResponse chat(String message, String conversationId) {
        final String resolvedId = resolveConversationId(conversationId);

        log.debug("FullAgent [{}]: {}", resolvedId, message);

        String reply = fullAgentClient.prompt()
                .user(message)
                .tools(dateTimeTool, calculatorTool, translateTool, docParseTool, dbQueryTool)
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, resolvedId)
                )
                .call()
                .content();

        log.debug("FullAgent [{}] 回复: {}", resolvedId, reply);

        return AgentResponse.builder()
                .conversationId(resolvedId)
                .reply(reply)
                .model("deepseek-v4-flash")
                .agentType("full")
                .build();
    }

    private String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return "full-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }
        return conversationId;
    }
}
