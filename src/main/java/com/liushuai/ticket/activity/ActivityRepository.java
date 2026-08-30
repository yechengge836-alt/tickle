// 声明活动持久化层所在的包。
package com.liushuai.ticket.activity;

// 导入可能为空的查询结果包装类型。
import java.util.Optional;
// 导入 Spring 的轻量级 JDBC 客户端。
import org.springframework.jdbc.core.simple.JdbcClient;
// 标记为数据库访问仓储组件。
import org.springframework.stereotype.Repository;

// 负责读写 activity 表。
@Repository
public class ActivityRepository {
    // 保存执行 SQL 的 JDBC 客户端。
    private final JdbcClient jdbc;
    // 由 Spring 注入数据库客户端。
    public ActivityRepository(JdbcClient jdbc) {
        // 保存注入的 JDBC 客户端。
        this.jdbc = jdbc;
    }
    // 根据主键查找活动，允许查询结果为空。
    public Optional<Activity> findById(long id) {
        // 查询活动字段，并将下划线列别名映射为 record 的驼峰字段。
        return jdbc.sql("SELECT id,name,total_stock AS totalStock,available_stock AS availableStock,status,start_at AS startAt,end_at AS endAt FROM activity WHERE id=:id")
                // 绑定 SQL 中的活动 ID 参数。
                .param("id", id)
                // 将单行查询映射成 Activity 并返回 Optional。
                .query(Activity.class).optional();
    }
    // 在库存充足且活动在售时，原子地扣减数据库库存。
    public int decreaseDbStock(long id, int quantity) {
        // 利用 UPDATE 条件避免库存变成负数。
        return jdbc.sql("UPDATE activity SET available_stock=available_stock-:quantity WHERE id=:id AND status='ON_SALE' AND available_stock>=:quantity")
                // 绑定目标活动。
                .param("id", id)
                // 绑定需要扣减的数量。
                .param("quantity", quantity)
                // 执行更新并返回受影响行数。
                .update();
    }
}
