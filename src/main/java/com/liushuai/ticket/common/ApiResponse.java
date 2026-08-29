package com.liushuai.ticket.common;

public record ApiResponse<T>(int code, String message, T data) {
    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(0, "success", data); }
    public static <T> ApiResponse<T> fail(int code, String message) { return new ApiResponse<>(code, message, null); }
}
