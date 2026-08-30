// 声明订单事件监听器所在的包。
package com.liushuai.ticket.order;

// 导入日志接口。
import org.slf4j.Logger;
// 导入日志工厂。
import org.slf4j.LoggerFactory;
// 导入 Kafka 消息发送模板。
import org.springframework.kafka.core.KafkaTemplate;
// 导入 Spring 组件注解。
import org.springframework.stereotype.Component;
// 导入事务提交阶段枚举。
import org.springframework.transaction.event.TransactionPhase;
// 导入事务事件监听注解。
import org.springframework.transaction.event.TransactionalEventListener;

// 监听订单创建事件，并在事务提交后发送 Kafka 消息。
@Component
public class OrderEventListener {
    // 为本类创建结构化日志记录器。
    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    // 保存 Kafka 发送工具。
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    // 注入 Kafka 发送模板。
    public OrderEventListener(KafkaTemplate<Object, Object> kafkaTemplate) {
        // 保存模板。
        this.kafkaTemplate = kafkaTemplate;
    }
    // 只有数据库事务成功提交后才触发，避免发送“幽灵订单”消息。
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(OrderCreatedEvent event) {
        // 以订单 ID 作为 Kafka 键，将同订单事件路由到同一分区。
        kafkaTemplate.send("ticket-order-created", event.orderId().toString(), event);
        // 记录事件已提交给 Kafka 客户端。
        log.info("published ticket-order-created, orderId={}", event.orderId());
    }
}
