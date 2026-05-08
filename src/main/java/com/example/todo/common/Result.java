package com.example.todo.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回结果封装类
 *
 * 用于 Controller 层返回 JSON 数据，确保所有接口返回格式一致。
 *
 * @param <T> data 字段的类型，支持任意类型（如 List、Map、Entity 等）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /**
     * 状态码
     * 200 = 成功
     * 400 = 请求参数错误（如缺少必填字段）
     * 404 = 资源不存在
     * 500 = 服务器内部错误
     */
    private int code;

    /**
     * 提示信息
     * 成功时返回 "success"
     * 失败时返回具体的错误描述
     */
    private String message;

    /**
     * 返回的数据
     * 泛型 T，可以是任意类型：
     * - 单个实体（如 User）
     * - 列表（如 List<TodoItem>）
     * - 地图（如 Map<String, Object>）
     * - 简单类型（如 String、Integer）
     */
    private T data;

    /**
     * 快速创建成功响应
     *
     * @param data 返回的数据
     * @param <T> 数据类型
     * @return 封装好的 Result 对象，code=200, message="success"
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 快速创建成功响应（无数据）
     *
     * @return 封装好的 Result 对象，code=200, message="success", data=null
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 快速创建错误响应
     *
     * @param message 错误信息
     * @param <T> 泛型（固定为 Object，因为错误时 data 通常为空）
     * @return 封装好的 Result 对象，code=500, message=传入的错误信息, data=null
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 创建自定义错误码的响应
     *
     * @param code 错误码（如 400, 404）
     * @param message 错误信息
     * @param <T> 泛型
     * @return 封装好的 Result 对象
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}