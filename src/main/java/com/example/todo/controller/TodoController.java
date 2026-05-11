package com.example.todo.controller;

import com.example.todo.common.Result;
import com.example.todo.entity.TodoItem;
import com.example.todo.service.TodoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Todo 任务控制器
 *
 * @RestController 注解详解：
 *   - 是 @Controller 和 @ResponseBody 的组合注解
 *   - @Controller：标记该类为 Spring MVC 控制器（处理 HTTP 请求）
 *   - @ResponseBody：方法的返回值会自动序列化为 JSON/XML，而不是渲染视图模板
 *   - 所以 @RestController 所有方法都返回数据，而非页面
 *
 * 重构要点：
 *   - 原来直接注入 TodoItemMapper 操作数据库
 *   - 现在改为注入 TodoService，由 Service 层处理业务逻辑
 *   - Controller 的职责简化为：接收请求、调用 Service、返回结果
 */
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    /**
     * 构造器注入 TodoService（推荐方式）
     *
     * @param todoService 业务逻辑服务
     */
    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * 获取所有任务列表（支持按标题模糊搜索）
     *
     * @GetMapping 注解详解：
     *   - 映射 HTTP GET 请求到该方法
     *   - 路径为 "/api/todos"（因为类上有 @RequestMapping("/api/todos")）
     *   - 完整路径：GET http://localhost:8080/api/todos
     *   - GET 请求通常用于读取数据，不会修改服务器状态
     *
     * @RequestParam 注解详解：
     *   - 将 HTTP 查询参数绑定到方法参数
     *   - required = false：该参数可选，不传时不会报错
     *   - 请求示例：GET /api/todos?title=AI
     *
     * @param title 搜索关键字（可选）
     * @return 统一返回结果，包含任务列表
     */
    @GetMapping
    public Result<List<TodoItem>> list(@RequestParam(required = false) String title) {
        List<TodoItem> todoList = todoService.list(title);
        return Result.success(todoList);
    }

    /**
     * 新增一条任务
     *
     * @PostMapping 注解详解：
     *   - 映射 HTTP POST 请求到该方法
     *   - 常用于创建资源（INSERT 操作）
     *   - 完整路径：POST http://localhost:8080/api/todos
     *
     * @RequestBody 注解详解：
     *   - 将 HTTP 请求体中的 JSON 数据自动反序列化为 Java 对象
     *   - 前提：请求的 Content-Type 必须为 application/json
     *
     * @param todoItem 前端传入的任务对象
     * @return 统一返回结果，包含插入后生成的主键 ID
     */
    @PostMapping
    public Result<TodoItem> add(@RequestBody TodoItem todoItem) {
        TodoItem savedItem = todoService.add(todoItem);
        return Result.success(savedItem);
    }

    /**
     * 更新任务状态
     *
     * @PutMapping 注解详解：
     *   - 映射 HTTP PUT 请求到该方法
     *   - PUT 通常用于完整更新资源
     *   - 完整路径：PUT http://localhost:8080/api/todos/{id}
     *
     * @PathVariable 注解详解：
     *   - 将 URL 路径中的变量绑定到方法参数
     *   - 例如路径 /api/todos/5 中的 5 会被绑定到 Long id 参数
     *
     * @param id 要更新的任务 ID
     * @param todoItem 前端传入的更新数据
     * @return 统一返回结果
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody TodoItem todoItem) {
        todoService.update(id, todoItem);
        return Result.success("更新成功");
    }

    /**
     * 删除任务
     *
     * @DeleteMapping 注解详解：
     *   - 映射 HTTP DELETE 请求到该方法
     *   - 常用于删除资源
     *   - 完整路径：DELETE http://localhost:8080/api/todos/{id}
     *
     * @param id 要删除的任务 ID
     * @return 统一返回结果
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        todoService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 获取统计数据
     *
     * @return 总计、已完成、待办数量
     */
    @GetMapping("/stats")
    public Result<Map<String, Long>> stats() {
        return Result.success(todoService.getStatistics());
    }

    /**
     * 清空所有已完成任务
     */
    @DeleteMapping("/completed")
    public Result<String> clearCompleted() {
        todoService.clearCompleted();
        return Result.success("已清空所有已完成任务");
    }

    /**
     * 级联删除任务及其所有子任务
     */
    @DeleteMapping("/{id}/cascade")
    public Result<String> deleteCascade(@PathVariable Long id) {
        todoService.removeWithChildren(id);
        return Result.success("已级联删除该任务及其子任务");
    }

    /**
     * 拖拽排序：按传入的 ID 顺序批量更新 sort_order
     */
    @PutMapping("/reorder")
    public Result<String> reorder(@RequestBody List<Long> ids) {
        todoService.reorder(ids);
        return Result.success("排序更新成功");
    }

    /**
     * 获取回收站任务列表
     */
    @GetMapping("/trash")
    public Result<List<TodoItem>> trash() {
        return Result.success(todoService.getTrash());
    }

    /**
     * 还原任务及其子任务
     */
    @PutMapping("/{id}/restore")
    public Result<String> restore(@PathVariable Long id) {
        todoService.restore(id);
        return Result.success("还原成功");
    }

    /**
     * 物理删除任务（彻底删除，不可恢复）
     */
    @DeleteMapping("/{id}/permanent")
    public Result<String> permanentDelete(@PathVariable Long id) {
        todoService.permanentDelete(id);
        return Result.success("已彻底删除");
    }
}
