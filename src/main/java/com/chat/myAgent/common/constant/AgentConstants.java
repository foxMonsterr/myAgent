package com.chat.myAgent.common.constant;

/**
 * Agent 全局常量
 */
public class AgentConstants {

    private AgentConstants() {}

    // ==================== 模型相关 ====================
    public static final String DEFAULT_MODEL = "deepseek-v4-flash";
    public static final String QWEN_MODEL = "qwen-plus";
    public static final String QWEN_TURBO = "qwen-turbo";

    // ==================== 会话相关（阶段2使用） ====================
    public static final int MAX_MEMORY_SIZE = 20;  // 最大记忆轮数
    public static final int SESSION_TIMEOUT_MINUTES = 30;  // 会话超时时间

    // ==================== Prompt 角色相关 ====================
    public static final String SYSTEM_ROLE_DEFAULT = "你是一个智能助手，名叫 SmartAgent。";

    // ==================== 扩展：工具名称（阶段3使用） ====================
    public static final String TOOL_DATETIME = "datetime";
    public static final String TOOL_CALCULATOR = "calculator";
    public static final String TOOL_TRANSLATE = "translate";
    public static final String TOOL_DOC_PARSE = "doc_parse";
    public static final String TOOL_DB_QUERY = "db_query";
}
