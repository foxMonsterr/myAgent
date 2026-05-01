package com.chat.myAgent.common.exception;

import com.chat.myAgent.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 * 捕获所有异常，返回统一格式，避免接口返回原始报错堆栈
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理 Spring AI 相关异常（模型调用失败、API Key 无效等）
     */
    @ExceptionHandler({org.springframework.ai.retry.NonTransientAiException.class})
    public R<Void> handleAiException(Exception e) {
        log.error("AI 模型调用异常: {}", e.getMessage(), e);
        return R.modelError(e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public R<Void> handleValidException(Exception e) {
        log.warn("参数校验失败: {}", e.getMessage());
        return R.paramError("请求参数不合法");
    }

    /**
     * 兜底：处理所有未捕获异常
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("未知异常: {}", e.getMessage(), e);
        return R.fail("服务器内部错误，请稍后重试");
    }
}
