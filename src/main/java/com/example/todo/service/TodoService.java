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

    /**
     * 级联删除任务及其所有子任务
     */
    void removeWithChildren(Long id);

    /**
     * 拖拽排序：按传入的 ID 顺序批量更新 sort_order
     */
    void reorder(List<Long> ids);

    /**
     * 获取回收站中的任务列表（is_deleted = 1）
     */
    List<TodoItem> getTrash();

    /**
     * 还原任务及其所有子任务（is_deleted 设回 0）
     */
    void restore(Long id);

    /**
     * 物理删除任务（彻底删除，不可恢复）
     */
    void permanentDelete(Long id);

    /**
     * 递归删除任务及其所有后代子任务
     */
    void deleteTaskRecursive(Long id);

    /**
     * 获取回收站任务列表（QueryWrapper 方式）
     */
    List<TodoItem> getRecycleBin();
}
