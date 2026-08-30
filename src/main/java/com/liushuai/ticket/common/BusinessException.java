// 声明业务异常所在的包。
package com.liushuai.ticket.common;

// 定义可安全返回给接口调用方的业务异常。
public class BusinessException extends RuntimeException {
    // 保存与 HTTP 状态无关的业务错误码。
    private final int code;
    // 用错误码和提示信息构造异常。
    public BusinessException(int code, String message) {
        // 把提示信息交给父类保存。
        super(message);
        // 保存业务错误码。
        this.code = code;
    }
    // 暴露业务错误码给全局异常处理器。
    public int getCode() {
        // 返回构造时保存的错误码。
        return code;
    }
}
