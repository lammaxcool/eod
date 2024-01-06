package org.kpi.processor.postgres;

import org.kpi.model.Order;
import org.kpi.processor.OrdersProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DatabaseIngestOrderProcessor implements OrdersProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseIngestOrderProcessor.class);

    @Override
    public void process(Order order) {
        LOGGER.info("Order with id {} processed", order.orderId());
    }
}