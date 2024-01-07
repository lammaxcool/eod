package org.kpi.processor.postgres;

import org.kpi.model.Order;
import org.kpi.processor.OrdersProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DatabaseIngestOrderProcessor implements OrdersProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseIngestOrderProcessor.class);

    private final OrdersRepository ordersRepository;

    public DatabaseIngestOrderProcessor(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @Override
    public void process(Order order) {
        saveOrder(order);

        LOGGER.info("Order with id {} processed", order.orderId());
    }

    private void saveOrder(Order order) {
        var orderPo = new OrderPo()
                .setOrderId(order.orderId())
                .setOrderTimestamp(Instant.ofEpochMilli(order.timestamp()));

        ordersRepository.save(orderPo);
    }
}