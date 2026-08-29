package com.liushuai.ticket.auth;

import java.time.LocalDateTime;

public record StoredUser(long id, String username, String passwordHash, LocalDateTime createdAt) {
    public LoginUser toLoginUser() { return new LoginUser(id, username, createdAt); }
}
