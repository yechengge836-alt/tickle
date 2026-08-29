package com.liushuai.ticket.auth;

import java.time.LocalDateTime;

public record LoginUser(long id, String username, LocalDateTime createdAt) { }
