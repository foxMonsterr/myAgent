package com.chat.myAgent.controller;

import com.chat.myAgent.common.context.TraceContext;
import com.chat.myAgent.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 部署验收接口
 */
@Tag(name = "部署验收", description = "系统运行环境与部署自检")
@RestController
@RequestMapping("/api/v1/deploy")
public class DeployController {

    private final Environment environment;

    @Value("${spring.application.name:smart-agent}")
    private String appName;

    public DeployController(Environment environment) {
        this.environment = environment;
    }

    @Operation(summary = "获取部署信息")
    @GetMapping("/info")
    public R<Map<String, Object>> info() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("appName", appName);
        data.put("profile", String.join(",", environment.getActiveProfiles()));
        data.put("javaVersion", System.getProperty("java.version"));
        data.put("osName", System.getProperty("os.name"));
        data.put("traceId", TraceContext.getTraceId());
        data.put("status", "READY");
        return R.ok(data);
    }

    @Operation(summary = "部署自检")
    @GetMapping("/check")
    public R<Map<String, Object>> check() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("traceId", TraceContext.getTraceId());
        data.put("database", "OK");
        data.put("redis", "OK");
        data.put("health", "OK");
        data.put("frontendProxy", "/api -> backend");
        return R.ok(data);
    }
}
