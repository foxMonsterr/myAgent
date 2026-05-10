package com.chat.myAgent.agent;

import com.chat.myAgent.common.context.TraceContext;
import com.chat.myAgent.model.vo.KnowledgeResponse;
import com.chat.myAgent.rag.RetrievalService;
import com.chat.myAgent.service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * RAG 知识库问答 Agent
 *
 * 核心能力：
 * 1. 基于私有知识库回答问题（而非模型通用知识）
 * 2. 支持多轮追问（记住对话上下文）
 * 3. 答案来源追溯（标注引用了哪些文档）
 *
 * 工作方式：
 * - QuestionAnswerAdvisor 自动完成检索和Prompt增强
 * - 用户只需正常提问，Agent内部自动查找知识库
 * - 如果知识库没有相关信息，会诚实告知
 */
@Slf4j
@Component
public class RagAgent {

    private final ChatClient ragChatClient;
    private final ChatClient baseChatClient;
    private final RetrievalService retrievalService;
    private final AuditService auditService;

    public RagAgent(
            @Qualifier("ragChatClient") ChatClient ragChatClient,
            @Qualifier("baseChatClient") ChatClient baseChatClient,
            RetrievalService retrievalService,
            AuditService auditService) {
        this.ragChatClient = ragChatClient;
        this.baseChatClient = baseChatClient;
        this.retrievalService = retrievalService;
        this.auditService = auditService;
    }

    /**
     * 知识库问答（自动RAG，使用 QuestionAnswerAdvisor）
     *
     * QuestionAnswerAdvisor 会自动：
     * 1. 将用户问题向量化
     * 2. 在向量库中搜索相关片段
     * 3. 将片段注入Prompt
     * 4. 模型基于片段回答
     */
    public KnowledgeResponse ask(String question, String conversationId) {
        final String resolvedConversationId = resolveConversationId(conversationId);

        log.debug("RagAgent [{}] 问题: {}", resolvedConversationId, question);

        // 使用 ragChatClient（已配置 QuestionAnswerAdvisor）
        String reply = ragChatClient.prompt()
                .user(question)
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, resolvedConversationId)
                )
                .call()
                .content();

        // 额外做一次检索，获取来源信息（用于展示给用户）
        List<Document> relatedDocs = retrievalService.retrieve(question);
        List<String> sources = retrievalService.getSourceFiles(relatedDocs);

        log.debug("RagAgent [{}] 回复 (引用{}个来源): {}", resolvedConversationId, sources.size(), reply);

        KnowledgeResponse response = KnowledgeResponse.builder()
                .conversationId(resolvedConversationId)
                .answer(reply)
                .sources(sources)
                .retrievedChunks(relatedDocs.size())
                .model("deepseek-v4-flash")
                .traceId(TraceContext.getTraceId())
                .build();
        auditService.saveAgentInvocation(resolvedConversationId, "rag", "deepseek-v4-flash", question, response.getAnswer(), null, "SUCCESS", 0L);
        return response;
    }

    /**
     * 知识库问答（手动RAG模式）
     *
     * 手动控制检索和Prompt拼接过程，适合需要自定义RAG逻辑的场景
     * 不使用 QuestionAnswerAdvisor，而是自己组装
     */
    public KnowledgeResponse askManual(String question, String conversationId) {
        conversationId = resolveConversationId(conversationId);

        log.debug("RagAgent(Manual) [{}] 问题: {}", conversationId, question);

        // 1. 手动检索
        List<Document> relatedDocs = retrievalService.retrieve(question);
        List<String> sources = retrievalService.getSourceFiles(relatedDocs);

        if (relatedDocs.isEmpty()) {
            KnowledgeResponse response = KnowledgeResponse.builder()
                    .conversationId(conversationId)
                    .answer("抱歉，知识库中暂无与您问题相关的信息。请尝试上传相关文档后再提问。")
                    .sources(List.of())
                    .retrievedChunks(0)
                    .model("deepseek-v4-flash")
                    .traceId(TraceContext.getTraceId())
                    .build();
            auditService.saveAgentInvocation(conversationId, "rag-manual", "deepseek-v4-flash", question, response.getAnswer(), null, "SUCCESS", 0L);
            return response;
        }

        // 2. 手动拼接上下文
        StringBuilder context = new StringBuilder();
        context.append("以下是从知识库中检索到的相关参考资料：\n\n");
        for (int i = 0; i < relatedDocs.size(); i++) {
            Document doc = relatedDocs.get(i);
            String source = (String) doc.getMetadata().getOrDefault("source", "未知");
            context.append("[参考").append(i + 1).append("] 来源: ").append(source).append("\n");
            context.append(doc.getText()).append("\n\n");
        }
        context.append("---\n请基于以上参考资料回答用户的问题。如果参考资料中没有相关信息，请说明。\n");

        // 3. 发送给模型
        String reply = baseChatClient.prompt()
                .system(context.toString())
                .user(question)
                .call()
                .content();

        log.debug("RagAgent(Manual) [{}] 回复: {}", conversationId, reply);

        KnowledgeResponse response = KnowledgeResponse.builder()
                .conversationId(conversationId)
                .answer(reply)
                .sources(sources)
                .retrievedChunks(relatedDocs.size())
                .model("deepseek-v4-flash")
                .traceId(TraceContext.getTraceId())
                .build();
        auditService.saveAgentInvocation(conversationId, "rag-manual", "deepseek-v4-flash", question, response.getAnswer(), null, "SUCCESS", 0L);
        return response;
    }

    /**
     * 纯检索（不生成回答，只返回检索到的文档片段）
     *
     * 用于调试：查看用户问题能检索到哪些片段
     */
    public String searchOnly(String query) {
        return retrievalService.retrieveFormatted(query);
    }

    private String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return "rag-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }
        return conversationId;
    }
}
