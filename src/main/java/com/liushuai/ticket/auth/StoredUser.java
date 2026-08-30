// 声明数据库用户模型所在的包。
package com.liushuai.ticket.auth;

// 导入创建时间使用的类型。
import java.time.LocalDateTime;

// 定义数据库读取的完整用户记录，包含仅供认证比较的密码哈希。
public record StoredUser(long id, String username, String passwordHash, LocalDateTime createdAt) {
    // 转换为不会暴露密码哈希的用户视图。
    public LoginUser toLoginUser() {
        // 只拷贝允许返回给客户端的字段。
        return new LoginUser(id, username, createdAt);
    }
}
