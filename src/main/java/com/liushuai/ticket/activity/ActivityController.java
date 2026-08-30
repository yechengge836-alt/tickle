// 声明活动接口所在的包。
package com.liushuai.ticket.activity;

// 导入统一接口响应类型。
import com.liushuai.ticket.common.ApiResponse;
// 导入用于返回库存键值对的 Map 类型。
import java.util.Map;
// 导入 GET 请求映射注解。
import org.springframework.web.bind.annotation.GetMapping;
// 导入路径变量绑定注解。
import org.springframework.web.bind.annotation.PathVariable;
// 导入控制器根路径映射注解。
import org.springframework.web.bind.annotation.RequestMapping;
// 导入 REST 控制器注解。
import org.springframework.web.bind.annotation.RestController;

// 标记此类为返回 JSON 的控制器。
@RestController
// 为本类的接口声明统一访问前缀。
@RequestMapping("/api/v1/activities")
// 定义活动查询接口。
public class ActivityController {
    // 保存处理活动业务的服务对象。
    private final ActivityService service;
    // 由 Spring 注入活动服务。
    public ActivityController(ActivityService service) {
        // 保存构造器注入的服务。
        this.service = service;
    }
    // 将 GET /{id} 映射为活动详情查询。
    @GetMapping("/{id}")
    public ApiResponse<Activity> detail(@PathVariable long id) {
        // 查询活动并包装成成功响应。
        return ApiResponse.ok(service.get(id));
    }
    // 将 GET /{id}/stock 映射为实时库存查询。
    @GetMapping("/{id}/stock")
    public ApiResponse<Map<String, Integer>> stock(@PathVariable long id) {
        // 以稳定的字段名返回当前可售库存。
        return ApiResponse.ok(Map.of("availableStock", service.getStock(id)));
    }
}
