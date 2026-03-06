package net.kafkajava.stock_service.config;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    /**
     * Configures a Dead Letter Queue (DLQ) error handler.
     * - Retries the failed message up to 3 times with a 1-second interval.
     * - After all retries are exhausted, the message is forwarded to the DLT topic
     * (e.g., "order_topics.DLT") so it is not silently lost.
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    LOGGER.error("[stock-service] Failed to process message after retries. " +
                            "Sending to DLT. Cause: {}", ex.getMessage());
                    return new TopicPartition(record.topic() + ".DLT", record.partition());
                });
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
    }
}
