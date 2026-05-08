package com.example.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.todo.entity.TodoItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * TodoItem Mapper 接口
 *
 * 继承 MyBatis-Plus 的 BaseMapper<T> 后，自动获得以下常用 CRUD 方法：
 * - insert(T entity)           插入一条记录
 * - deleteById(Serializable id)  根据 ID 删除
 * - updateById(T entity)      根据 ID 更新
 * - selectById(Serializable id)  根据 ID 查询
 * - selectList(wrapper)       条件查询列表
 * - ...更多方法
 *
 * 无需编写 XML 或 SQL，MyBatis-Plus 会根据实体类自动生成 SQL。
 *
 * Mapper 接口无需实现类，MyBatis-Plus 会在运行时自动生成代理对象。
 */
@Mapper  // 标记为 MyBatis Mapper，Spring 会自动扫描并注入
public interface TodoItemMapper extends BaseMapper<TodoItem> {
    // BaseMapper 已提供所有基础 CRUD 方法，此处无需额外定义
}