package com.chat.myAgent.controller;

import com.chat.myAgent.common.context.TraceContext;
import com.chat.myAgent.common.result.R;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
public class SystemInfoController {

    private final Environment environment;

    public SystemInfoController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/info")
    public R<Map<String, Object>> info() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("application", environment.getProperty("spring.application.name", "smart-agent"));
        data.put("activeProfiles", String.join(",", environment.getActiveProfiles()));
        data.put("javaVersion", System.getProperty("java.version"));
        data.put("traceId", TraceContext.getTraceId());
        return R.ok(data);
    }
}
