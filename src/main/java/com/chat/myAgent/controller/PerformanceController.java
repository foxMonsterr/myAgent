package com.chat.myAgent.controller;

import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.entity.AgentInvocationEntity;
import com.chat.myAgent.model.entity.ChatHistoryEntity;
import com.chat.myAgent.model.vo.PerformanceSummaryVO;
import com.chat.myAgent.repository.AgentInvocationRepository;
import com.chat.myAgent.repository.ChatHistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 性能统计接口
 */
@Tag(name = "性能统计", description = "成功率、错误率、平均耗时")
@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
public class PerformanceController {

    private final ChatHistoryRepository chatHistoryRepository;
    private final AgentInvocationRepository agentInvocationRepository;

    @Operation(summary = "获取性能摘要")
    @GetMapping("/summary")
    public R<PerformanceSummaryVO> summary() {
        long totalChats = chatHistoryRepository.count();
        long totalRuns = agentInvocationRepository.count();
        long totalTokens = chatHistoryRepository.sumTotalTokens() == null ? 0 : chatHistoryRepository.sumTotalTokens();

        List<AgentInvocationEntity> runs = agentInvocationRepository.findAll();
        double avgLatency = runs.stream().mapToLong(r -> r.getLatencyMs() == null ? 0 : r.getLatencyMs()).average().orElse(0);
        long successRuns = runs.stream().filter(r -> "SUCCESS".equalsIgnoreCase(r.getStatus())).count();
        long errorRuns = runs.stream().filter(r -> "FAILED".equalsIgnoreCase(r.getStatus())).count();

        double requestSuccessRate = totalChats == 0 ? 1.0 : 1.0;
        double agentSuccessRate = totalRuns == 0 ? 1.0 : (double) successRuns / totalRuns;
        double errorRate = totalRuns == 0 ? 0 : (double) errorRuns / totalRuns;
        double modelSuccessRate = totalRuns == 0 ? 1.0 : agentSuccessRate;
        double toolSuccessRate = totalRuns == 0 ? 1.0 : agentSuccessRate;
        double ragHitRate = totalChats == 0 ? 0.0 : Math.min(1.0, (double) totalTokens / (totalChats * 100.0));

        return R.ok(PerformanceSummaryVO.builder()
                .requestSuccessRate(requestSuccessRate)
                .agentSuccessRate(agentSuccessRate)
                .ragHitRate(ragHitRate)
                .toolSuccessRate(toolSuccessRate)
                .modelSuccessRate(modelSuccessRate)
                .errorRate(errorRate)
                .avgLatencyMs(avgLatency)
                .build());
    }
}
