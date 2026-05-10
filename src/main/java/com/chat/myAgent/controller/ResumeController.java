package com.chat.myAgent.controller;

import com.chat.myAgent.common.result.R;
import com.chat.myAgent.model.vo.ResumeProjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 简历项目描述接口
 */
@Tag(name = "简历描述", description = "输出可直接用于简历的项目描述")
@RestController
@RequestMapping("/api/v1/resume")
public class ResumeController {

    @Operation(summary = "获取简历项目描述")
    @GetMapping("/project")
    public R<ResumeProjectVO> project() {
        return R.ok(ResumeProjectVO.builder()
                .projectName("企业级 AI 智能体平台")
                .summary("基于 Spring Boot + Spring AI 构建的企业级 AI 平台，支持多轮对话、Agent 工具调用、RAG 检索、任务规划、流式输出、JWT/RBAC 权限控制、审计追踪与监控看板。")
                .highlights(List.of(
                        "支持思考模式、专家对话和多轮上下文记忆",
                        "实现 Agent 工具调用、知识检索增强和任务规划",
                        "通过 traceId、审计日志和监控看板实现全链路可观测",
                        "支持 Docker 部署和前后端分离控制台展示"
                ))
                .techStack(List.of("Spring Boot", "Spring AI", "Spring Security", "JPA", "Redis", "Vue 3", "Element Plus", "Pinia", "TypeScript"))
                .responsibilities(List.of(
                        "设计并实现多 Agent 编排与统一模型调用链路",
                        "构建 RAG 知识库、工具调用与任务规划能力",
                        "完善 JWT + RBAC 权限体系、审计与监控",
                        "搭建前端控制台和演示中心，提升可展示性"
                ))
                .build());
    }
}
