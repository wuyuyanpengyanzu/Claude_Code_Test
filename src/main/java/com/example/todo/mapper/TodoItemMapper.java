package com.example.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.todo.entity.TodoItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * TodoItem Mapper 接口
 *
 * 继承 MyBatis-Plus 的 BaseMapper<T> 后，自动获得以下常用 CRUD 方法：
 * - insert(T entity)           插入一条记录
 * - deleteById(Serializable id)  根据 ID 逻辑删除（@TableLogic）
 * - updateById(T entity)      根据 ID 更新
 * - selectById(Serializable id)  根据 ID 查询
 * - selectList(wrapper)       条件查询列表
 * - ...更多方法
 *
 * 注意：@TableLogic 会自动为 select/delete 追加 is_deleted=0 条件，
 * 因此 trash/restore 操作需用自定义 SQL 绕过该自动过滤。
 */
@Mapper
public interface TodoItemMapper extends BaseMapper<TodoItem> {

    /** 查询回收站中所有已逻辑删除的任务（绕过 @TableLogic 自动过滤） */
    @Select("SELECT * FROM todo_item WHERE is_deleted = 1 ORDER BY parent_id ASC, sort_order ASC, is_starred DESC, status ASC, id DESC")
    List<TodoItem> selectTrashList();

    /** 逻辑还原任务及其所有子任务 */
    @Update("UPDATE todo_item SET is_deleted = 0 WHERE id = #{id} OR parent_id = #{id}")
    int restoreCascade(@Param("id") Long id);

    /** 物理删除单条任务（绕过 @TableLogic） */
    @Delete("DELETE FROM todo_item WHERE id = #{id}")
    int deletePermanent(@Param("id") Long id);

    /** 物理删除任务及其所有子任务 */
    @Delete("DELETE FROM todo_item WHERE id = #{id} OR parent_id = #{id}")
    int deletePermanentCascade(@Param("id") Long id);

    /** 统计回收站中指定 ID 的任务（用于校验任务是否在回收站中） */
    @Select("SELECT COUNT(*) FROM todo_item WHERE id = #{id} AND is_deleted = 1")
    int countTrashById(@Param("id") Long id);
}