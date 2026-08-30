// 声明认证建表初始化组件所在的包。
package com.liushuai.ticket.auth;

// 导入容器完成依赖注入后的回调注解。
import jakarta.annotation.PostConstruct;
// 导入执行 SQL 的 JDBC 客户端。
import org.springframework.jdbc.core.simple.JdbcClient;
// 导入 Spring 组件注解。
import org.springframework.stereotype.Component;

// 在应用启动时补建用户表，方便首次运行项目。
@Component
public class AuthSchemaInitializer {
    // 保存 JDBC 客户端。
    private final JdbcClient jdbc;

    // 注入 JDBC 客户端。
    public AuthSchemaInitializer(JdbcClient jdbc) {
        // 保存数据库操作对象。
        this.jdbc = jdbc;
    }

    // 在 Bean 初始化完成后确保用户表存在。
    @PostConstruct
    void ensureUserTable() {
        // 执行幂等建表 SQL；已存在时不会修改现有数据。
        jdbc.sql("""
                -- 创建保存平台账号的表。
                CREATE TABLE IF NOT EXISTS platform_user (
                  -- 用户主键，使用数据库自增值。
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  -- 登录账号，最长 19 个字符。
                  username VARCHAR(19) NOT NULL,
                  -- BCrypt 哈希后的密码，绝不保存明文。
                  password_hash VARCHAR(100) NOT NULL,
                  -- 记录创建时间。
                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  -- 禁止重复注册同一账号。
                  UNIQUE KEY uk_platform_user_username (username),
                  -- 用数据库约束再次保证账号长度。
                  CONSTRAINT ck_platform_user_username_length CHECK (CHAR_LENGTH(username) BETWEEN 6 AND 19)
                )
                -- 结束 DDL 语句。
                """).update();
    }
}
