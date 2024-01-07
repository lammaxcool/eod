package org.kpi.processor.postgres.redis;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile(value = {"redis", "test"})
public interface RedisOrdersRepository extends JpaRepository<RedisOrderPo, Long> {
}