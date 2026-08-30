// 声明活动领域模型所在的包。
package com.liushuai.ticket.activity;

// 导入活动开始和结束时间使用的 Java 时间类型。
import java.time.LocalDateTime;

// 不可变活动数据对象，与 activity 表字段一一对应。
public record Activity(Long id, String name, int totalStock, int availableStock,
                       String status, LocalDateTime startAt, LocalDateTime endAt) { }
