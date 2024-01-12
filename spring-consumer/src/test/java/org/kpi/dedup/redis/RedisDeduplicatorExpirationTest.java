package org.kpi.dedup.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.kpi.dedup.DeduplicationKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Objects;

@Testcontainers
@DirtiesContext
@EnableScheduling
@ActiveProfiles(profiles = {"test"})
@ContextConfiguration(classes = {RedisDeduplicatorExpirationTest.LocalTestConfiguration.class, DeduplicationKeysExpireTicker.class})
@SpringBootTest(properties = {"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"}, classes = {RedisDeduplicator.class})
class RedisDeduplicatorExpirationTest {

    public static final Duration EXPIRE_DURATION_MILLIS = Duration.ofMillis(300);

    @Container
    static RedisContainer REDIS_CONTAINER = new RedisContainer();

    @Autowired
    RedisDeduplicator redisDeduplicator;

    @Autowired
    StringRedisTemplate redisTemplate;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);

        registry.add("application.redis.expire-duration-millis", EXPIRE_DURATION_MILLIS::toMillis);
    }

    @AfterEach
    void tearDown() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushAll();
    }

    @Test
    void expireMembers() {
        var deduplicationKey = new DeduplicationKey("key1", String.valueOf(1));
        var status = redisDeduplicator.checkAndSet(deduplicationKey);
        assertTrue(status);

        await()
                .atMost(Duration.ofSeconds(3))
                .until(() -> redisDeduplicator.isUnique(deduplicationKey));

        status = redisDeduplicator.checkAndSet(deduplicationKey);
        assertTrue(status);
    }

    @Test
    void expireKeys() {
        var deduplicationKey = new DeduplicationKey("key1", String.valueOf(1));
        var status = redisDeduplicator.checkAndSet(deduplicationKey);
        assertTrue(status);

        // as there is no any calls to such partition
        // it will be removed after last record ttl
        await()
                .atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> assertThat(redisTemplate.hasKey(deduplicationKey.key())).isFalse());
    }

    @TestConfiguration
    @Import(value = {RedisConfiguration.class, DeduplicatorRedisConfiguration.class})
    static class LocalTestConfiguration {
    }
}