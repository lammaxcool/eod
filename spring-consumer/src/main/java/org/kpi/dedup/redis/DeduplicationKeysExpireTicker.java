package org.kpi.dedup.redis;

import org.kpi.dedup.Deduplicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile(value = {"redis", "test"})
public class DeduplicationKeysExpireTicker {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeduplicationKeysExpireTicker.class);

    private final Deduplicator<?> deduplicator;

    public DeduplicationKeysExpireTicker(RedisDeduplicator deduplicator) {
        this.deduplicator = deduplicator;
    }

    @Scheduled(initialDelayString = "${application.redis.expire-duration-millis}", fixedRateString = "${application.redis.expire-duration-millis}")
    private void expireDuplicatedKeys() {
        LOGGER.info("Trying to expire duplicated keys...");
        deduplicator.expireDuplicatedKeys();
    }
}