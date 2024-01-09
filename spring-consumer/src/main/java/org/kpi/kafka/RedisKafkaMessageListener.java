package org.kpi.kafka;

import org.kpi.dedup.DeduplicationKey;
import org.kpi.dedup.Deduplicator;
import org.kpi.model.Order;
import org.kpi.processor.OrdersProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
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

    @RetryableTopic(backoff = @Backoff(delay = 1000, multiplier = 1.5))
    @KafkaListener(topics = "${application.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(Order message, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        LOGGER.info("Got new message from partition: {}, message: {}", partition, message);

        var deduplicationKey = deduplicationKey(message, partition);
        if (deduplicator.checkAndSet(deduplicationKey)) {
            try {
                ordersProcessor.process(message);
            } catch (Exception ex) {
                LOGGER.error("Error occurred during message processing, message: {}, trying to rollback...", message, ex);
                deduplicator.remove(deduplicationKey);
            }
        }
    }

    @DltHandler
    public void dltHandler(Order message) {
        LOGGER.error("Got message in DLT: {})", message);
    }

    private static DeduplicationKey deduplicationKey(Order message, int partition) {
        return new DeduplicationKey(String.valueOf(message.orderId()), String.valueOf(partition));
    }
}