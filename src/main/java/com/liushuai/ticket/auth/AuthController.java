// 声明认证接口所在的包。
package com.liushuai.ticket.auth;

// 导入统一接口响应。
import com.liushuai.ticket.common.ApiResponse;
// 导入请求参数校验注解。
import jakarta.validation.Valid;
// 导入创建资源时使用的 HTTP 状态码。
import org.springframework.http.HttpStatus;
// 导入 POST 映射注解。
import org.springframework.web.bind.annotation.PostMapping;
// 导入 JSON 请求体绑定注解。
import org.springframework.web.bind.annotation.RequestBody;
// 导入控制器路径前缀注解。
import org.springframework.web.bind.annotation.RequestMapping;
// 导入成功响应状态注解。
import org.springframework.web.bind.annotation.ResponseStatus;
// 导入 REST 控制器注解。
import org.springframework.web.bind.annotation.RestController;

// 标记为认证 REST 控制器。
@RestController
// 为注册和登录接口设置公共前缀。
@RequestMapping("/api/v1/auth")
public class AuthController {
    // 保存认证业务服务。
    private final AuthService service;

    // 通过构造器注入认证服务。
    public AuthController(AuthService service) {
        // 保存注入的服务实例。
        this.service = service;
    }

    // 接收用户注册请求。
    @PostMapping("/register")
    // 注册成功时按 REST 语义返回 201。
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LoginUser> register(@Valid @RequestBody AuthRequest request) {
        // 校验请求后创建用户，并用标准响应包装。
        return ApiResponse.ok(service.register(request));
    }

    // 接收用户登录请求。
    @PostMapping("/login")
    public ApiResponse<LoginSession> login(@Valid @RequestBody AuthRequest request) {
        // 验证账号密码后返回登录会话与令牌。
        return ApiResponse.ok(service.login(request));
    }
}
