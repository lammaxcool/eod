package org.kpi.processor.postgres.eos;

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
@Table(name = "orders_eos", schema = "eos")
@EntityListeners(AuditingEntityListener.class)
public class EosOrderPo {

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

    public EosOrderPo setId(long id) {
        this.id = id;
        return this;
    }

    public long orderId() {
        return orderId;
    }

    public EosOrderPo setOrderId(long orderId) {
        this.orderId = orderId;
        return this;
    }

    public Instant orderTimestamp() {
        return orderTimestamp;
    }

    public EosOrderPo setOrderTimestamp(Instant orderTimestamp) {
        this.orderTimestamp = orderTimestamp;
        return this;
    }

    public Instant processedAt() {
        return processedAt;
    }

    public EosOrderPo setProcessedAt(Instant processedAt) {
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
        EosOrderPo redisOrderPo = (EosOrderPo) o;
        return id == redisOrderPo.id
                && orderId == redisOrderPo.orderId
                && Objects.equals(orderTimestamp, redisOrderPo.orderTimestamp)
                && Objects.equals(processedAt, redisOrderPo.processedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, orderTimestamp, processedAt);
    }
}