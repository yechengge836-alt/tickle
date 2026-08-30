// 声明订单持久化层所在的包。
package com.liushuai.ticket.order;

// 导入可选查询结果类型。
import java.util.Optional;
// 导入 JDBC 客户端。
import org.springframework.jdbc.core.simple.JdbcClient;
// 标记为 Spring 数据仓储组件。
import org.springframework.stereotype.Repository;

// 负责 ticket_order 表的查询和写入。
@Repository
public class OrderRepository {
    // 保存 SQL 执行客户端。
    private final JdbcClient jdbc;
    // 注入 JDBC 客户端。
    public OrderRepository(JdbcClient jdbc) {
        // 保存依赖对象。
        this.jdbc = jdbc;
    }
    // 用请求幂等键查找已有订单。
    public Optional<TicketOrder> findByRequestId(String requestId) {
        // 查询订单列，并转换数据库下划线名称为 Java 驼峰名称。
        return jdbc.sql("SELECT id,request_id AS requestId,activity_id AS activityId,user_id AS userId,quantity,status,created_at AS createdAt FROM ticket_order WHERE request_id=:requestId")
                // 绑定幂等键参数。
                .param("requestId", requestId)
                // 返回可能不存在的订单。
                .query(TicketOrder.class).optional();
    }
    // 查询某用户在某活动中的订单，用于每人限购一次。
    public Optional<TicketOrder> findByUserAndActivity(long userId, long activityId) {
        // 根据用户和活动联合条件查询订单。
        return jdbc.sql("SELECT id,request_id AS requestId,activity_id AS activityId,user_id AS userId,quantity,status,created_at AS createdAt FROM ticket_order WHERE user_id=:userId AND activity_id=:activityId")
                // 绑定用户 ID。
                .param("userId", userId)
                // 绑定活动 ID。
                .param("activityId", activityId)
                // 映射为可选订单。
                .query(TicketOrder.class).optional();
    }
    // 插入一条新建状态的订单。
    public long insert(CreateOrderRequest request) {
        // 写入请求幂等键、活动、用户和购买数量。
        return jdbc.sql("INSERT INTO ticket_order(request_id,activity_id,user_id,quantity,status) VALUES(:requestId,:activityId,:userId,:quantity,'CREATED')")
                // 绑定请求唯一标识。
                .param("requestId", request.requestId())
                // 绑定活动 ID。
                .param("activityId", request.activityId())
                // 绑定登录用户 ID。
                .param("userId", request.userId())
                // 绑定购票数量。
                .param("quantity", request.quantity())
                // 执行插入并返回受影响行数。
                .update();
    }
}
