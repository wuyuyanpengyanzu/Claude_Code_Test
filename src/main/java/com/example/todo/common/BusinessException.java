package com.example.todo.common;

/**
 * 业务异常
 *
 * 用于表示业务规则被违反（如子任务层级限制）。
 * 由 GlobalExceptionHandler 统一拦截，返回 code=400 的错误响应。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
