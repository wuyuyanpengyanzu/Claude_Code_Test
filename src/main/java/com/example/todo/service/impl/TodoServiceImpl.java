package com.example.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.todo.entity.TodoItem;
import com.example.todo.mapper.TodoItemMapper;
import com.example.todo.service.TodoService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Todo 任务服务实现类
 *
 * @Service 注解详解：
 *   - 标记该类为 Spring 的 Service 层组件
 *   - Spring 会自动扫描并创建该类的单例对象
 *   - 可以在 Controller 中通过 @Autowired 或构造器注入
 *
 * Service 层的职责：
 *   1. 业务逻辑处理（校验、计算、组合数据等）
 *   2. 调用 Mapper 操作数据库
 *   3. 事务管理（@Transactional）
 *   4. 异常处理与转换
 */
@Service
public class TodoServiceImpl implements TodoService {

    private final TodoItemMapper todoItemMapper;

    /**
     * 构造器注入 Mapper（推荐方式）
     *
     * 相比 @Autowired 字段注入，构造器注入的优势：
     * 1. 不可变性：final 字段确保一旦赋值不会被修改
     * 2. 测试友好：可以显式传入 mock 的 Mapper
     * 3. 明确依赖：所有依赖在构造时就确定
     */
    public TodoServiceImpl(TodoItemMapper todoItemMapper) {
        this.todoItemMapper = todoItemMapper;
    }

    @Override
    public List<TodoItem> list(String title) {
        QueryWrapper<TodoItem> wrapper = new QueryWrapper<>();

        // 如果传了 title 参数，按标题模糊搜索
        // wrapper.like("title", title) 生成 SQL: WHERE title LIKE '%关键字%'
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like("title", title);
        }

        // 按状态升序（未完成 0 在前），再按 id 倒序
        wrapper.orderByAsc("status").orderByDesc("id");

        return todoItemMapper.selectList(wrapper);
    }

    @Override
    public TodoItem add(TodoItem todoItem) {
        // 参数校验：title 不能为空
        if (todoItem.getTitle() == null || todoItem.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("标题不能为空");
        }

        // 状态默认值处理：如果前端没有传 status，默认设为 0（未完成）
        if (todoItem.getStatus() == null) {
            todoItem.setStatus(0);
        }

        // 插入数据库
        int rows = todoItemMapper.insert(todoItem);

        if (rows > 0) {
            // 插入成功后返回包含主键 ID 的对象
            return todoItem;
        } else {
            throw new RuntimeException("新增失败");
        }
    }

    @Override
    public void update(Long id, TodoItem todoItem) {
        // 查询该 ID 是否存在
        TodoItem existingItem = todoItemMapper.selectById(id);
        if (existingItem == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        // 只更新前端传入的字段（防止覆盖数据库已有数据）
        if (todoItem.getTitle() != null) {
            existingItem.setTitle(todoItem.getTitle());
        }
        if (todoItem.getContent() != null) {
            existingItem.setContent(todoItem.getContent());
        }
        if (todoItem.getStatus() != null) {
            existingItem.setStatus(todoItem.getStatus());
        }

        int rows = todoItemMapper.updateById(existingItem);
        if (rows == 0) {
            throw new RuntimeException("更新失败");
        }
    }

    @Override
    public void delete(Long id) {
        // 查询该 ID 是否存在
        TodoItem existingItem = todoItemMapper.selectById(id);
        if (existingItem == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        int rows = todoItemMapper.deleteById(id);
        if (rows == 0) {
            throw new RuntimeException("删除失败");
        }
    }

    //
    @Override
    public Map<String, Long> getStatistics() {
        long total = todoItemMapper.selectCount(null);
        long completed = todoItemMapper.selectCount(
                new QueryWrapper<TodoItem>().eq("status", 1));
        long pending = todoItemMapper.selectCount(
                new QueryWrapper<TodoItem>().eq("status", 0));

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("completed", completed);
        stats.put("pending", pending);
        return stats;
    }

    @Override
    public void clearCompleted() {
        todoItemMapper.delete(new QueryWrapper<TodoItem>().eq("status", 1));
    }
}
