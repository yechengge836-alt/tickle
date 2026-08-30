// 声明认证请求模型所在的包。
package com.liushuai.ticket.auth;

// 导入不能为空校验注解。
import jakarta.validation.constraints.NotBlank;
// 导入字符串长度校验注解。
import jakarta.validation.constraints.Size;

// 定义注册和登录共用的请求体，并在边界处限制账号、密码长度。
public record AuthRequest(
        // 账号不能为空且长度必须为 6 到 19 个字符。
        @NotBlank(message = "账号不能为空") @Size(min = 6, max = 19, message = "账号长度必须为 6-19 个字符") String username,
        // 密码不能为空且长度必须为 6 到 19 个字符。
        @NotBlank(message = "密码不能为空") @Size(min = 6, max = 19, message = "密码长度必须为 6-19 个字符") String password
) { }
