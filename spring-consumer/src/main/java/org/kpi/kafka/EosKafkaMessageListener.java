package org.kpi.kafka;

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
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("eos")
public class EosKafkaMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EosKafkaMessageListener.class);

    private final OrdersProcessor ordersProcessor;

    public EosKafkaMessageListener(OrdersProcessor ordersProcessor) {
        this.ordersProcessor = ordersProcessor;
    }

    @Transactional
    @RetryableTopic(backoff = @Backoff(delay = 1000, multiplier = 1.5))
    @KafkaListener(topics = "${application.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(Order message, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        LOGGER.info("Got new message from partition: {}, message: {}", partition, message);
        ordersProcessor.process(message);
    }

    @DltHandler
    public void dltHandler(Order message) {
        LOGGER.error("Got message in DLT: {})", message);
    }
}