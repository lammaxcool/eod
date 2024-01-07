package org.kpi.processor.postgres;

import org.kpi.model.Order;
import org.kpi.processor.OrdersProcessor;
import org.kpi.processor.postgres.redis.RedisOrderPo;
import org.kpi.processor.postgres.redis.RedisOrdersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;

@Component
public class DatabaseIngestOrderProcessor implements OrdersProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseIngestOrderProcessor.class);

    private final RedisOrdersRepository redisOrdersRepository;

    public DatabaseIngestOrderProcessor(RedisOrdersRepository redisOrdersRepository) {
        this.redisOrdersRepository = redisOrdersRepository;
    }

    @Override
    public void process(Order order) {
        saveOrder(order);

        LOGGER.info("Order with id {} processed", order.orderId());
    }

    private void saveOrder(Order order) {
        var orderPo = new RedisOrderPo()
                .setOrderId(order.orderId())
                .setOrderTimestamp(shiftToSystemInstant(order.timestamp()));

        redisOrdersRepository.save(orderPo);
    }

    private static Instant shiftToSystemInstant(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toInstant();
    }
}