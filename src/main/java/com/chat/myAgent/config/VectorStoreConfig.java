package com.chat.myAgent.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 向量库配置
 *
 * 设计说明：
 * - 阶段4使用 SimpleVectorStore（基于内存+文件持久化）
 * - SimpleVectorStore 将向量数据保存为 JSON 文件，重启后可恢复
 * - 如果后续需要更强大的向量库，可以切换为 Redis Vector / Milvus / PGVector
 *
 * SimpleVectorStore 特性：
 * - 基于余弦相似度计算
 * - 支持持久化到本地 JSON 文件
 * - 适合中小规模文档（万级以内）
 * - 无需额外安装任何数据库
 */
@Slf4j
@Configuration
public class VectorStoreConfig {

    /**
     * 向量库文件存储路径
     * 从配置文件读取，默认值为 ./data/vectorstore/vector-store.json
     */
    @Value("${smart-agent.rag.vector-store-path:./data/vectorstore/vector-store.json}")
    private String vectorStorePath;

    /**
     * 创建并配置 VectorStore Bean
     *
     * 工作流程：
     * 1. 接收 Spring AI 自动配置的 EmbeddingModel（用于将文本转换为向量）
     * 2. 确保向量库文件的父目录存在，不存在则创建
     * 3. 构建 SimpleVectorStore 实例
     * 4. 如果向量库文件已存在且非空，则加载历史数据
     * 5. 返回配置好的 VectorStore，供 RAG 功能使用
     *
     * @param embeddingModel 嵌入模型，由 Spring AI 根据 application.yml 自动配置
     *                       当前使用阿里云 DashScope 的 text-embedding-v4 模型
     * @return 配置好的 VectorStore 实例
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // 记录正在初始化的向量库类型和使用的嵌入模型
        log.info("正在初始化 SimpleVectorStore, embeddingModel={}", embeddingModel.getClass().getSimpleName());

        // 将配置的路径字符串转换为 Path 对象，便于后续文件操作
        Path path = Paths.get(vectorStorePath);

        // 获取向量库文件的父目录
        File dir = path.getParent().toFile();

        // 如果父目录不存在，递归创建所有必需的目录
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 使用 Builder 模式创建 SimpleVectorStore 实例
        // 传入 embeddingModel 用于后续的向量计算（文本→向量、相似度匹配等）
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();

        // 获取向量库文件对象，用于后续的文件检查和数据加载
        File storeFile = path.toFile();

        // 记录向量库文件的详细信息，便于调试和监控
        log.info("向量库文件路径: {}, 文件存在: {}, 文件大小: {} bytes",
                storeFile.getAbsolutePath(), storeFile.exists(),
                storeFile.exists() ? storeFile.length() : 0);

        // 判断向量库文件是否存在且非空
        if (storeFile.exists() && storeFile.length() > 0) {
            // 文件存在且有内容，尝试加载历史向量数据
            try {
                store.load(storeFile);
                log.info("向量库加载成功");
            } catch (Exception e) {
                // 加载失败时记录错误日志，但不中断应用启动
                // 失败后会创建一个新的空向量库
                log.error("向量库加载失败: {}", e.getMessage(), e);
            }
        } else {
            // 文件不存在或为空，这是首次运行或数据被清空的情况
            // SimpleVectorStore 会在首次保存时自动创建文件
            log.warn("向量库文件不存在或为空，将创建新的向量库");
        }

        // 返回配置好的 VectorStore Bean
        // Spring 容器会将其注入到需要使用向量库的组件中（如 RagAgent、DocumentIngestor 等）
        return store;
    }
}