package com.liushuai.ticket.order;

public record OrderCreatedEvent(Long orderId, Long activityId, Long userId, Integer quantity) { }
