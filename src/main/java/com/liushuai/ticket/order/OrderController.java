// 声明订单接口所在的包。
package com.liushuai.ticket.order;

// 导入登录用户解析服务。
import com.liushuai.ticket.auth.AuthService;
// 导入统一接口响应类型。
import com.liushuai.ticket.common.ApiResponse;
// 导入请求体校验注解。
import jakarta.validation.Valid;
// 导入创建资源时返回的状态码。
import org.springframework.http.HttpStatus;
// 导入 POST 请求映射注解。
import org.springframework.web.bind.annotation.PostMapping;
// 导入 JSON 请求体绑定注解。
import org.springframework.web.bind.annotation.RequestBody;
// 导入路径映射注解。
import org.springframework.web.bind.annotation.RequestMapping;
// 导入请求头读取注解。
import org.springframework.web.bind.annotation.RequestHeader;
// 导入响应状态设置注解。
import org.springframework.web.bind.annotation.ResponseStatus;
// 导入 REST 控制器注解。
import org.springframework.web.bind.annotation.RestController;

// 标记为订单 REST 控制器。
@RestController
// 声明订单接口的公共访问路径。
@RequestMapping("/api/v1/ticket-orders")
public class OrderController {
    // 保存下单业务服务。
    private final OrderService service;
    // 保存登录令牌校验服务。
    private final AuthService authService;
    // 通过构造器注入下单和认证依赖。
    public OrderController(OrderService service, AuthService authService) {
        // 保存下单服务。
        this.service = service;
        // 保存认证服务。
        this.authService = authService;
    }
    // 将 POST 根路径映射为创建订单。
    @PostMapping
    // 成功创建订单时按 REST 语义返回 201。
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TicketOrder> create(@RequestHeader(value = "X-Auth-Token", required = false) String accessToken,
                                             @Valid @RequestBody CreateOrderRequest request) {
        // 使用令牌中的真实用户 ID 重建请求，阻止客户端伪造 userId。
        CreateOrderRequest authenticatedRequest = new CreateOrderRequest(request.activityId(), authService.requireUserId(accessToken), request.quantity(), request.requestId());
        // 执行创建订单逻辑并统一包装响应。
        return ApiResponse.ok(service.create(authenticatedRequest));
    }
}
