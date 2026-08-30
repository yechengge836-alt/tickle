// 声明创建订单请求模型所在的包。
package com.liushuai.ticket.order;

// 导入最大值校验注解。
import jakarta.validation.constraints.Max;
// 导入最小值校验注解。
import jakarta.validation.constraints.Min;
// 导入文本不能为空校验注解。
import jakarta.validation.constraints.NotBlank;
// 导入对象不能为空校验注解。
import jakarta.validation.constraints.NotNull;
// 导入文本长度校验注解。
import jakarta.validation.constraints.Size;

// 定义创建订单的请求字段及其接口层校验规则。
public record CreateOrderRequest(
        // 必须指定要购买的活动。
        @NotNull Long activityId,
        // 该字段会被控制器用登录用户 ID 覆盖，不能信任客户端传值。
        @NotNull Long userId,
        // 单次购买数量限制为 1 到 2 张。
        @Min(1) @Max(2) Integer quantity,
        // 请求幂等键不能为空且最长 64 字符。
        @NotBlank @Size(max = 64) String requestId) { }
