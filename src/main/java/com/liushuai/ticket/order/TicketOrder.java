package com.liushuai.ticket.order;

import java.time.LocalDateTime;

public record TicketOrder(Long id, String requestId, Long activityId, Long userId,
                          Integer quantity, String status, LocalDateTime createdAt) { }
