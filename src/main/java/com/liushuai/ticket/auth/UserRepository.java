// 声明用户持久化层所在的包。
package com.liushuai.ticket.auth;

// 导入可选查询结果包装类型。
import java.util.Optional;
// 导入 Spring JDBC 客户端。
import org.springframework.jdbc.core.simple.JdbcClient;
// 标记为数据库访问仓储。
import org.springframework.stereotype.Repository;

// 负责 platform_user 表的查询和新增。
@Repository
public class UserRepository {
    // 保存执行 SQL 的对象。
    private final JdbcClient jdbc;

    // 通过构造器注入 JDBC 客户端。
    public UserRepository(JdbcClient jdbc) {
        // 保存数据库访问对象。
        this.jdbc = jdbc;
    }

    // 按账号查询完整用户记录，用于注册查重和登录验密。
    public Optional<StoredUser> findByUsername(String username) {
        // 查询密码哈希并为下划线列提供驼峰别名。
        return jdbc.sql("SELECT id, username, password_hash AS passwordHash, created_at AS createdAt FROM platform_user WHERE username=:username")
                // 绑定账号参数。
                .param("username", username)
                // 映射为 StoredUser，未查到时返回空 Optional。
                .query(StoredUser.class).optional();
    }

    // 新建用户并返回不含密码的安全用户信息。
    public LoginUser insert(String username, String passwordHash) {
        // 插入账号与 BCrypt 密码哈希。
        jdbc.sql("INSERT INTO platform_user(username, password_hash) VALUES(:username, :passwordHash)")
                // 绑定账号。
                .param("username", username)
                // 绑定哈希而非明文密码。
                .param("passwordHash", passwordHash)
                // 执行插入。
                .update();
        // 再次查询由数据库生成的 ID 与创建时间。
        return jdbc.sql("SELECT id, username, created_at AS createdAt FROM platform_user WHERE username=:username")
                // 绑定刚刚插入的账号。
                .param("username", username)
                // 读取唯一一行并映射为安全视图。
                .query(LoginUser.class).single();
    }
}
