package com.liushuai.ticket.auth;

import java.time.LocalDateTime;

public record LoginSession(long id, String username, LocalDateTime createdAt, String accessToken) { }
