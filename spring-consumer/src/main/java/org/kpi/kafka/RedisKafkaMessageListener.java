package org.kpi.kafka;

import org.kpi.dedup.DeduplicationKey;
import org.kpi.dedup.Deduplicator;
import org.kpi.model.Order;
import org.kpi.processor.OrdersProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Profile(value = {"redis", "test"})
public class RedisKafkaMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisKafkaMessageListener.class);

    private final Deduplicator<DeduplicationKey> deduplicator;
    private final OrdersProcessor ordersProcessor;

    public RedisKafkaMessageListener(Deduplicator<DeduplicationKey> deduplicator, OrdersProcessor ordersProcessor) {
        this.deduplicator = deduplicator;
        this.ordersProcessor = ordersProcessor;
    }

    @KafkaListener(topics = "${application.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(Order message, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        LOGGER.info("Got new message from partition: {}, message: {}", partition, message);

        if (deduplicator.checkAndSet(deduplicationKey(message, partition))) {
            try {
                ordersProcessor.process(message);
            } catch (Exception ex) {
                LOGGER.error("Error occurred during message processing, message: {}, trying to rollback...", message, ex);
                // TODO: remove key from deduplicator
                // TODO: send to dead letters
            }
        }
    }

    private static DeduplicationKey deduplicationKey(Order message, int partition) {
        return new DeduplicationKey(String.valueOf(message.orderId()), String.valueOf(partition));
    }
}