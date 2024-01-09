package org.kpi.processor.postgres;

import org.kpi.model.Order;
import org.kpi.processor.OrdersProcessor;
import org.kpi.processor.postgres.eos.EosOrderPo;
import org.kpi.processor.postgres.eos.EosOrdersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;

@Component
@Profile(value = {"eos"})
public class EosDatabaseIngestOrderProcessor implements OrdersProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EosDatabaseIngestOrderProcessor.class);

    private final EosOrdersRepository eosOrdersRepository;

    public EosDatabaseIngestOrderProcessor(EosOrdersRepository eosOrdersRepository) {
        this.eosOrdersRepository = eosOrdersRepository;
    }

    @Override
    public void process(Order order) throws ProcessingException {
        try {
            saveOrder(order);
            LOGGER.info("Order with id {} processed", order.orderId());
        } catch (Exception ex) {
            LOGGER.error("Error occurred during  processing order with id: {}", order.orderId(), ex);
            throw new ProcessingException(ex);
        }
    }

    private void saveOrder(Order order) {
        var orderPo = new EosOrderPo()
                .setOrderId(order.orderId())
                .setOrderTimestamp(shiftToSystemInstant(order.timestamp()));

        eosOrdersRepository.save(orderPo);
    }

    private static Instant shiftToSystemInstant(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toInstant();
    }
}