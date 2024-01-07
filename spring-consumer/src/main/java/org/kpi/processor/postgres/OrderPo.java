package org.kpi.processor.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "orders", schema = "public")
@EntityListeners(AuditingEntityListener.class)
public class OrderPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Column(nullable = false)
    private long orderId;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant orderTimestamp;

    @NotNull
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant processedAt;

    public long id() {
        return id;
    }

    public OrderPo setId(long id) {
        this.id = id;
        return this;
    }

    public long orderId() {
        return orderId;
    }

    public OrderPo setOrderId(long orderId) {
        this.orderId = orderId;
        return this;
    }

    public Instant orderTimestamp() {
        return orderTimestamp;
    }

    public OrderPo setOrderTimestamp(Instant orderTimestamp) {
        this.orderTimestamp = orderTimestamp;
        return this;
    }

    public Instant processedAt() {
        return processedAt;
    }

    public OrderPo setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderPo orderPo = (OrderPo) o;
        return id == orderPo.id
                && orderId == orderPo.orderId
                && Objects.equals(orderTimestamp, orderPo.orderTimestamp)
                && Objects.equals(processedAt, orderPo.processedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, orderTimestamp, processedAt);
    }
}