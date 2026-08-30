// 声明订单领域模型所在的包。
package com.liushuai.ticket.order;

// 导入订单创建时间使用的类型。
import java.time.LocalDateTime;

// 定义 ticket_order 表对应的不可变订单数据对象。
public record TicketOrder(Long id, String requestId, Long activityId, Long userId,
                          Integer quantity, String status, LocalDateTime createdAt) { }
