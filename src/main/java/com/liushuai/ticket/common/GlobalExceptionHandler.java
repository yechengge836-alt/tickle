// 声明全局异常处理器所在的包。
package com.liushuai.ticket.common;

// 导入 Bean Validation 约束异常。
import jakarta.validation.ConstraintViolationException;
// 导入 HTTP 响应包装类型。
import org.springframework.http.ResponseEntity;
// 导入异常类型到处理方法的映射注解。
import org.springframework.web.bind.annotation.ExceptionHandler;
// 导入 REST 全局增强注解。
import org.springframework.web.bind.annotation.RestControllerAdvice;
// 导入请求体参数校验失败异常。
import org.springframework.web.bind.MethodArgumentNotValidException;

// 拦截所有 REST 控制器抛出的异常并统一返回 JSON。
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 专门处理预期内的业务异常。
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse<Void>> business(BusinessException e) {
        // 用业务异常自带的错误码和信息返回 400 响应。
        return ResponseEntity.badRequest().body(ApiResponse.fail(e.getCode(), e.getMessage()));
    }
    // 处理 @Valid 与路径参数等校验失败。
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    ResponseEntity<ApiResponse<Void>> validation(Exception e) {
        // 避免把内部校验细节暴露给前端。
        return ResponseEntity.badRequest().body(ApiResponse.fail(400, "请求参数不合法"));
    }
    // 兜底处理未预期的系统异常。
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> unknown(Exception e) {
        // 返回通用提示，防止泄露数据库或堆栈信息。
        return ResponseEntity.internalServerError().body(ApiResponse.fail(500, "系统繁忙，请稍后重试"));
    }
}
