package org.kpi.processor;

import org.kpi.model.Order;

public interface OrdersProcessor {

    void process(Order order);

}