package com.liushuai.ticket.activity;

import java.time.LocalDateTime;

public record Activity(Long id, String name, int totalStock, int availableStock,
                       String status, LocalDateTime startAt, LocalDateTime endAt) { }
