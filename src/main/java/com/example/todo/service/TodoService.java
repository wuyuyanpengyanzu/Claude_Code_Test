package com.example.todo.service;

import com.example.todo.entity.TodoItem;

import java.util.List;
import java.util.Map;

/**
 * Todo 任务服务接口
 *
 * 定义业务逻辑的契约，实现类负责具体实现。
 * 接口与实现分离的好处：
 * 1. 可以随时替换实现类而不影响调用方
 * 2. 方便编写单元测试（可 mock）
 */
public interface TodoService {

    /**
     * 获取任务列表（支持按标题模糊搜索）
     */
    List<TodoItem> list(String title);

    /**
     * 新增任务
     */
    TodoItem add(TodoItem todoItem);

    /**
     * 更新任务
     */
    void update(Long id, TodoItem todoItem);

    /**
     * 删除任务
     */
    void delete(Long id);

    /**
     * 获取统计数据（总计、已完成、待办）
     */
    Map<String, Long> getStatistics();

    /**
     * 清空所有已完成任务
     */
    void clearCompleted();
}
