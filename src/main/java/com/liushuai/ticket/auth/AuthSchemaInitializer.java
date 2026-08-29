package com.liushuai.ticket.auth;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class AuthSchemaInitializer {
    private final JdbcClient jdbc;

    public AuthSchemaInitializer(JdbcClient jdbc) { this.jdbc = jdbc; }

    @PostConstruct
    void ensureUserTable() {
        jdbc.sql("""
                CREATE TABLE IF NOT EXISTS platform_user (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  username VARCHAR(19) NOT NULL,
                  password_hash VARCHAR(100) NOT NULL,
                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  UNIQUE KEY uk_platform_user_username (username),
                  CONSTRAINT ck_platform_user_username_length CHECK (CHAR_LENGTH(username) BETWEEN 6 AND 19)
                )
                """).update();
    }
}
