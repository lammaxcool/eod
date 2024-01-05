package org.kpi.kafka;

import org.kpi.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaMessageListener.class);

    @KafkaListener(topics = "${application.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(Order message, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        LOGGER.info("Got new message from partition: {}, message: {}", partition, message);
    }
}