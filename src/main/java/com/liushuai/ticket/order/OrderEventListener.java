package com.liushuai.ticket.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    public OrderEventListener(KafkaTemplate<Object, Object> kafkaTemplate) { this.kafkaTemplate = kafkaTemplate; }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(OrderCreatedEvent event) {
        kafkaTemplate.send("ticket-order-created", event.orderId().toString(), event);
        log.info("published ticket-order-created, orderId={}", event.orderId());
    }
}
