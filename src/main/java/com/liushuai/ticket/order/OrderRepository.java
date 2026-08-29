package com.liushuai.ticket.order;

import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {
    private final JdbcClient jdbc;
    public OrderRepository(JdbcClient jdbc) { this.jdbc = jdbc; }
    public Optional<TicketOrder> findByRequestId(String requestId) {
        return jdbc.sql("SELECT id,request_id AS requestId,activity_id AS activityId,user_id AS userId,quantity,status,created_at AS createdAt FROM ticket_order WHERE request_id=:requestId")
                .param("requestId", requestId).query(TicketOrder.class).optional();
    }
    public Optional<TicketOrder> findByUserAndActivity(long userId, long activityId) {
        return jdbc.sql("SELECT id,request_id AS requestId,activity_id AS activityId,user_id AS userId,quantity,status,created_at AS createdAt FROM ticket_order WHERE user_id=:userId AND activity_id=:activityId")
                .param("userId", userId).param("activityId", activityId).query(TicketOrder.class).optional();
    }
    public long insert(CreateOrderRequest request) {
        return jdbc.sql("INSERT INTO ticket_order(request_id,activity_id,user_id,quantity,status) VALUES(:requestId,:activityId,:userId,:quantity,'CREATED')")
                .param("requestId", request.requestId()).param("activityId", request.activityId())
                .param("userId", request.userId()).param("quantity", request.quantity())
                .update();
    }
}
