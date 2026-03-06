package net.kafkajava.email_service.kafka;

import net.kafkajava.base_domains.dto.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class OrderConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumer.class);

    // In-memory idempotency store — prevents duplicate emails being sent.
    // NOTE: For multi-instance deployments, replace with a Redis-backed set.
    private final Set<String> processedOrderIds = Collections.synchronizedSet(new HashSet<>());

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(OrderEvent event) {
        String orderId = event.getOrder().getOrderId();

        if (processedOrderIds.contains(orderId)) {
            LOGGER.warn("[email-service] Duplicate event ignored for orderId: {}", orderId);
            return;
        }

        processedOrderIds.add(orderId);
        LOGGER.info("[email-service] Order event received => {}", event);
        // TODO: send an email to the customer
    }
}
