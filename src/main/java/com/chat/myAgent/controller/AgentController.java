package com.chat.myAgent.controller;

import com.chat.myAgent.agent.ToolAgent;
import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.dto.AgentRequest;
import com.chat.myAgent.model.vo.AgentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Agent 工具调用接口
 *
 * 这是阶段3的核心接口，让AI具备调用外部工具的能力
 */
@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
public class AgentController {

    private final ToolAgent toolAgent;

    /**
     * 工具调用对话（无记忆）
     * POST /api/v1/agent/chat
     *
     * AI 会根据用户问题自主决定是否调用工具
     *
     * 测试用例：
     * - "现在几点了" → 调用 DateTimeTool
     * - "128乘以256等于多少" → 调用 CalculatorTool
     * - "把'你好世界'翻译成英文" → 调用 TranslateTool
     * - "读取 sample.md 文件" → 调用 DocParseTool
     * - "查一下技术部有哪些员工" → 调用 DbQueryTool
     * - "今天天气怎么样" → 不调用工具，直接回答
     */
    @PostMapping("/chat")
    public R<AgentResponse> chat(@Valid @RequestBody AgentRequest request) {
        AgentResponse response = toolAgent.chat(request);
        return R.ok(response);
    }

    /**
     * 工具调用对话（带记忆）
     * POST /api/v1/agent/chat/memory
     *
     * 在工具调用基础上，增加多轮记忆
     * 适合需要上下文的连续工具调用场景
     */
    @PostMapping("/chat/memory")
    public R<AgentResponse> chatWithMemory(@Valid @RequestBody AgentRequest request) {
        AgentResponse response = toolAgent.chatWithMemory(request);
        return R.ok(response);
    }

    /**
     * 指定工具对话
     * POST /api/v1/agent/chat/specific
     *
     * 只启用请求中指定的工具，限制AI的工具调用范围
     *
     * 请求示例：
     * {
     *   "message": "128*256等于多少",
     *   "tools": ["calculator", "datetime"]
     * }
     */
    @PostMapping("/chat/specific")
    public R<AgentResponse> chatWithSpecificTools(@Valid @RequestBody AgentRequest request) {
        if (request.getTools() == null || request.getTools().isEmpty()) {
            // 未指定工具时，使用全量工具
            return R.ok(toolAgent.chat(request));
        }
        String[] toolNames = request.getTools().toArray(new String[0]);
        AgentResponse response = toolAgent.chatWithSpecificTools(request, toolNames);
        return R.ok(response);
    }
}
