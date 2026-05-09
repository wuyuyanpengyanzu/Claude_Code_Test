package com.example.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.todo.common.BusinessException;
import com.example.todo.entity.TodoItem;
import com.example.todo.mapper.TodoItemMapper;
import com.example.todo.service.TodoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        LambdaQueryWrapper<TodoItem> wrapper = new LambdaQueryWrapper<>();

        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(TodoItem::getTitle, title);
        }

        // 先按 parent_id 升序（顶级任务在前），再按星标、状态、ID 排序
        wrapper.orderByAsc(TodoItem::getParentId)
                .orderByDesc(TodoItem::getIsStarred)
                .orderByAsc(TodoItem::getStatus)
                .orderByDesc(TodoItem::getId);

        List<TodoItem> allItems = todoItemMapper.selectList(wrapper);

        // 构建父子树：分离顶级任务和子任务，将子任务挂到对应父任务下
        Map<Long, List<TodoItem>> childrenMap = allItems.stream()
                .filter(item -> item.getParentId() != null)
                .collect(Collectors.groupingBy(TodoItem::getParentId));

        List<TodoItem> tree = new ArrayList<>();
        for (TodoItem item : allItems) {
            if (item.getParentId() == null) {
                item.setChildren(childrenMap.getOrDefault(item.getId(), new ArrayList<>()));
                tree.add(item);
            }
        }
        return tree;
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

        // 二级结构约束：如果指定了 parentId，校验父任务本身不是子任务
        if (todoItem.getParentId() != null) {
            TodoItem parentItem = todoItemMapper.selectById(todoItem.getParentId());
            if (parentItem == null) {
                throw new BusinessException("父任务不存在");
            }
            if (parentItem.getParentId() != null) {
                throw new BusinessException("不能在子任务下继续添加子任务");
            }
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
        if(todoItem.getIsStarred() != null){
            existingItem.setIsStarred(todoItem.getIsStarred());
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
        todoItemMapper.delete(new LambdaQueryWrapper<TodoItem>().eq(TodoItem::getStatus,1));
    }

    @Override
    @Transactional
    public void removeWithChildren(Long id) {
        TodoItem existingItem = todoItemMapper.selectById(id);
        if (existingItem == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        // 1. 先删除所有子任务（parent_id = id）
        todoItemMapper.delete(
                new LambdaQueryWrapper<TodoItem>().eq(TodoItem::getParentId, id));

        // 2. 再删除父任务自身
        todoItemMapper.deleteById(id);
    }
}
