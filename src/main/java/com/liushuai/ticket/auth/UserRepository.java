package com.liushuai.ticket.auth;

import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final JdbcClient jdbc;

    public UserRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

    public Optional<StoredUser> findByUsername(String username) {
        return jdbc.sql("SELECT id, username, password_hash AS passwordHash, created_at AS createdAt FROM platform_user WHERE username=:username")
                .param("username", username).query(StoredUser.class).optional();
    }

    public LoginUser insert(String username, String passwordHash) {
        jdbc.sql("INSERT INTO platform_user(username, password_hash) VALUES(:username, :passwordHash)")
                .param("username", username).param("passwordHash", passwordHash).update();
        return jdbc.sql("SELECT id, username, created_at AS createdAt FROM platform_user WHERE username=:username")
                .param("username", username).query(LoginUser.class).single();
    }
}
