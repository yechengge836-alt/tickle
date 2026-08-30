// 声明订单事件模型所在的包。
package com.liushuai.ticket.order;

// 定义订单创建成功后发布给异步消费者的事件内容。
public record OrderCreatedEvent(Long orderId, Long activityId, Long userId, Integer quantity) { }
