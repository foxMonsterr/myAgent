package com.chat.myAgent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 多模型配置
 *
 * 支持在配置文件中预定义多个模型，接口调用时动态切换
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "smart-agent.models")
public class ModelConfig {

    /**
     * 默认模型名称
     */
    private String defaultModel = "deepseek-v4-flash";

    /**
     * 可用模型列表
     * key: 模型别名
     * value: 实际模型名称
     */
    private Map<String, String> available = new LinkedHashMap<>();

    /**
     * 获取实际模型名称
     */
    public String resolveModel(String alias) {
        if (alias == null || alias.isBlank()) {
            return defaultModel;
        }
        return available.getOrDefault(alias, alias);
    }
}
