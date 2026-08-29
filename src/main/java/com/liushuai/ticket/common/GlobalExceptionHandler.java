package com.liushuai.ticket.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse<Void>> business(BusinessException e) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(e.getCode(), e.getMessage()));
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    ResponseEntity<ApiResponse<Void>> validation(Exception e) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(400, "请求参数不合法"));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> unknown(Exception e) {
        return ResponseEntity.internalServerError().body(ApiResponse.fail(500, "系统繁忙，请稍后重试"));
    }
}
