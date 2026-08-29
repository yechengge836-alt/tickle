package com.liushuai.ticket.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    @KafkaListener(topics = "ticket-order-created")
    public void onOrderCreated(OrderCreatedEvent event) {
        // Integration point for SMS / e-mail / in-app notifications.
        log.info("consume order event: orderId={}, userId={}", event.orderId(), event.userId());
    }
}
