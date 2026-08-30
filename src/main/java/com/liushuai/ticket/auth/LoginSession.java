// 声明登录会话模型所在的包。
package com.liushuai.ticket.auth;

// 导入创建时间使用的类型。
import java.time.LocalDateTime;

// 定义登录成功后返回的用户信息和访问令牌。
public record LoginSession(long id, String username, LocalDateTime createdAt, String accessToken) { }
