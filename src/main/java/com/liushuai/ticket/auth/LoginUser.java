// 声明安全用户视图所在的包。
package com.liushuai.ticket.auth;

// 导入创建时间使用的类型。
import java.time.LocalDateTime;

// 定义可对外返回的用户信息，不包含密码哈希。
public record LoginUser(long id, String username, LocalDateTime createdAt) { }
