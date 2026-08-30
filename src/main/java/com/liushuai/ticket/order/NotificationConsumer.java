// 声明订单通知消费者所在的包。
package com.liushuai.ticket.order;

// 导入日志接口。
import org.slf4j.Logger;
// 导入日志工厂。
import org.slf4j.LoggerFactory;
// 导入 Kafka 消息监听注解。
import org.springframework.kafka.annotation.KafkaListener;
// 导入 Spring 组件注解。
import org.springframework.stereotype.Component;

// 订阅订单创建主题，作为短信、邮件或站内信的扩展入口。
@Component
public class NotificationConsumer {
    // 为本类创建日志记录器。
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    // 监听 Kafka 的订单创建主题。
    @KafkaListener(topics = "ticket-order-created")
    public void onOrderCreated(OrderCreatedEvent event) {
        // 此处可接入短信、邮件或站内信服务；当前仅记录消费日志。
        log.info("consume order event: orderId={}, userId={}", event.orderId(), event.userId());
    }
}
