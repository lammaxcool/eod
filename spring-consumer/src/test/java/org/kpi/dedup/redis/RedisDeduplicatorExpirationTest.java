package org.kpi.dedup.redis;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@Testcontainers
@SpringBootTest
@DirtiesContext
@EnableScheduling
@ActiveProfiles(profiles = {"test"})
class RedisDeduplicatorExpirationTest {

    public static final Duration EXPIRE_DURATION_MILLIS = Duration.ofMillis(1000);

    @Container
    static RedisContainer REDIS_CONTAINER = new RedisContainer();

    @Autowired
    RedisDeduplicator redisDeduplicator;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);

        registry.add("application.redis.expire-duration-millis", EXPIRE_DURATION_MILLIS::toMillis);
    }

    @Test
    void expireMembers() {
        var deduplicationKey = new DeduplicationKey("key1", String.valueOf(1));
        var status = redisDeduplicator.checkAndSet(deduplicationKey);
        assertTrue(status);

        await()
                .atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> assertTrue(redisDeduplicator.isUnique(deduplicationKey)));

        status = redisDeduplicator.checkAndSet(deduplicationKey);
        assertTrue(status);
    }
}