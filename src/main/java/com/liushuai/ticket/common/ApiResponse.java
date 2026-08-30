// 声明通用响应模型所在的包。
package com.liushuai.ticket.common;

// 用 record 定义统一接口响应：业务码、提示信息和实际数据。
public record ApiResponse<T>(int code, String message, T data) {
    // 创建业务成功的响应，0 代表成功。
    public static <T> ApiResponse<T> ok(T data) {
        // 把调用方的数据装入标准响应体。
        return new ApiResponse<>(0, "success", data);
    }
    // 创建不携带数据的失败响应。
    public static <T> ApiResponse<T> fail(int code, String message) {
        // 保留业务错误码和可展示的错误信息。
        return new ApiResponse<>(code, message, null);
    }
}
