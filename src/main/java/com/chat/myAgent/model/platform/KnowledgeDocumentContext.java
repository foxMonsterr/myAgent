package com.chat.myAgent.model.platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识文档上下文
 *
 * 统一描述知识库文档的元信息，便于后续做多知识库、版本管理、审计展示。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocumentContext {

    /**
     * 文档 ID
     */
    private String documentId;

    /**
     * 知识库名称
     */
    private String knowledgeBase;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件类型，例如 md / txt / pdf / docx
     */
    private String fileType;

    /**
     * 文件大小
     */
    private long fileSize;

    /**
     * 片段数量
     */
    private int chunkCount;

    /**
     * 文档标签
     */
    private List<String> tags;

    /**
     * 是否已发布
     */
    private boolean published;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
