package org.kpi.processor.postgres.eos;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile(value = {"eos"})
public interface EosOrdersRepository extends JpaRepository<EosOrderPo, Long> {
}