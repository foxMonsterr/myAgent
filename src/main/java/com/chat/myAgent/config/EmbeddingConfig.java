package com.chat.myAgent.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Embedding 模型配置
 *
 * 使用阿里云 DashScope（Qwen）的 Embedding 服务
 * 通过 OpenAI 兼容模式调用
 *
 * 支持的模型：
 * - text-embedding-v3：通用场景首选，支持 1024/768/512 等维度
 * - text-embedding-v4：最新旗舰，支持 2048/1536 高维向量
 */
@Configuration
public class EmbeddingConfig {

    @Value("${dashscope.api-key}")
    private String dashScopeApiKey;

    @Value("${dashscope.base-url}")
    private String dashScopeBaseUrl;

    @Value("${dashscope.embedding-model:text-embedding-v3}")
    private String embeddingModel;

    @Bean
    public OpenAiEmbeddingModel embeddingModel() {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(dashScopeBaseUrl)
                .apiKey(dashScopeApiKey)
                .embeddingsPath("/embeddings")
                .build();

        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(embeddingModel)
                .build();

        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
    }
}
