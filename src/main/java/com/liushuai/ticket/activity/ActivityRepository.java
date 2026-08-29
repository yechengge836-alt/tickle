package com.liushuai.ticket.activity;

import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ActivityRepository {
    private final JdbcClient jdbc;
    public ActivityRepository(JdbcClient jdbc) { this.jdbc = jdbc; }
    public Optional<Activity> findById(long id) {
        return jdbc.sql("SELECT id,name,total_stock AS totalStock,available_stock AS availableStock,status,start_at AS startAt,end_at AS endAt FROM activity WHERE id=:id")
                .param("id", id).query(Activity.class).optional();
    }
    public int decreaseDbStock(long id, int quantity) {
        return jdbc.sql("UPDATE activity SET available_stock=available_stock-:quantity WHERE id=:id AND status='ON_SALE' AND available_stock>=:quantity")
                .param("id", id).param("quantity", quantity).update();
    }
}
