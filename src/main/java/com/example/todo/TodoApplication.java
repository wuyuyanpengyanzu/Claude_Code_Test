package com.example.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TodoList 应用启动类
 *
 * @SpringBootApplication 是以下三个注解的组合：
 * 1. @Configuration：标记该类为配置类，可替代 XML 配置文件
 * 2. @EnableAutoConfiguration：启用 Spring Boot 的自动配置机制
 * 3. @ComponentScan：自动扫描 com.example.todo 包及其子包中的组件
 */
@SpringBootApplication
public class TodoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
    }
}