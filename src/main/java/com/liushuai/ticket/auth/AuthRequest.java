package com.liushuai.ticket.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank(message = "账号不能为空") @Size(min = 6, max = 19, message = "账号长度必须为 6-19 个字符") String username,
        @NotBlank(message = "密码不能为空") @Size(min = 6, max = 19, message = "密码长度必须为 6-19 个字符") String password
) { }
