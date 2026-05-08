package com.example.todo.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 引入 Slf4j 日志对象
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgument(IllegalArgumentException e) {
        // 2. 在控制台打印带标记的警告/信息日志
        log.warn("[全局异常拦截] 捕获到参数校验异常: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntime(RuntimeException e) {
        // 2. 在控制台打印错误堆栈或详细信息
        log.error("[全局异常拦截] 捕获到系统运行时异常: ", e);
        return Result.error(500, e.getMessage());
    }
}