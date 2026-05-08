package com.example.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Todo 项实体类
 *
 * ====================================== MyBatis-Plus Entity 与数据库表对应规则 ======================================
 *
 * MyBatis-Plus 通过 Entity 类与数据库表进行映射，默认遵循以下约定：
 *
 * 1. 表名默认规则：
 *    - 类名驼峰转下划线：TodoItem -> todo_item
 *    - 可以通过 @TableName 注解显式指定表名
 *
 * 2. 字段默认规则：
 *    - 属性名驼峰转下划线：userName -> user_name
 *    - 可以通过 @TableField 注解显式指定列名
 *    - 非数据库字段使用 @TableField(exist = false) 标记
 *
 * 3. 主键默认规则：
 *    - 默认使用 id 作为主键字段
 *    - 可以通过 @TableId 注解指定主键策略
 *
 * ====================================================== 注解详解 ======================================================
 *
 * @TableName("todo_item")
 *    - 用于标记实体类对应的数据库表名
 *    - 位置：类名上方
 *    - 若表名与类名转换规则一致，可省略此注解
 *
 * @TableId
 *    - 标记主键字段
 *    - type 属性指定主键生成策略：
 *      - IdType.AUTO：数据库自增（需要 DB 设置 AUTO_INCREMENT）
 *      - IdType.INPUT：手动输入
 *      - IdType.ASSIGN_ID：基于雪花算法生成 Long 类型 ID（默认）
 *      - IdType.ASSIGN_UUID：基于 UUID 生成字符串 ID
 *
 * @TableField
 *    - 用于标记非主键字段
 *    - exist 属性：标记字段是否存在于数据库表中
 *      - exist = true（默认）：该属性对应数据库列
 *      - exist = false：该属性不是数据库列（如 transient、computed 字段）
 *    - value 属性：指定数据库列名（当属性名与列名不一致时使用）
 *
 * ====================================================== 数据库表结构 ======================================================
 *
 * CREATE TABLE `todo_item` (
 *   `id` BIGINT NOT NULL AUTO_INCREMENT,     -- 主键，自增
 *   `title` VARCHAR(255) NOT NULL,           -- 标题，必填
 *   `content` TEXT,                          -- 内容，可为空
 *   `status` TINYINT DEFAULT 0,             -- 状态：0=未完成，1=已完成
 *   `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
 *   `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
 *   PRIMARY KEY (`id`)
 * );
 */
@Data  // Lombok 注解：自动生成 getter、setter、toString、equals、hashCode 等方法
@TableName("todo_item")  // 指定实体类对应的数据库表名
public class TodoItem {

    /**
     * 主键 ID
     *
     * @TableId 标记此字段为主键
     * type = IdType.AUTO 表示使用数据库自增策略
     * MySQL 中需要将该列设置为 AUTO_INCREMENT
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Todo 标题
     *
     * 属性名 title 与数据库列名 title 完全一致（都是下划线命名）
     * MyBatis-Plus 默认会将驼峰命名转为下划线，所以可以省略 @TableField 注解
     * 若列名不同，如数据库列为 todo_title，则需使用：@TableField("todo_title")
     */
    private String title;

    /**
     * Todo 内容
     *
     * 类型为 String，数据库对应 TEXT 类型
     * MyBatis-Plus 会自动处理 String <-> TEXT 的映射
     */
    private String content;

    /**
     * 状态
     *
     * 0 = 未完成
     * 1 = 已完成
     * 数据库类型为 TINYINT，Java 类型为 Integer
     * MyBatis-Plus 会自动进行类型转换
     */
    private Integer status;

    /**
     * 创建时间
     *
     * 数据库 DEFAULT CURRENT_TIMESTAMP 会自动设置默认值
     * Java 8+ 使用 LocalDateTime 替代 Date
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     *
     * ON UPDATE CURRENT_TIMESTAMP 会在更新时自动刷新
     * 建议在业务层或 MyBatis-Plus 自动填充功能中维护此字段
     */
    private LocalDateTime updateTime;

    /**
     * 演示非数据库字段
     *
     * 使用 @TableField(exist = false) 标记，表示该字段不是数据库列
     * 常见用途：前端展示用字段、计算字段、临时存储字段等
     */
    @TableField(exist = false)
    private String extraInfo;
}
