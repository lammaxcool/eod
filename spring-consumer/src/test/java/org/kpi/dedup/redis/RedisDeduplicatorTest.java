package org.kpi.dedup.redis;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kpi.dedup.DeduplicationKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Objects;

@Testcontainers
@SpringBootTest
@DirtiesContext
@ActiveProfiles(profiles = {"test"})
@ContextConfiguration(classes = {RedisDeduplicatorTest.LocalTestConfiguration.class})
class RedisDeduplicatorTest {

    @Container
    static RedisContainer REDIS_CONTAINER = new RedisContainer();

    @Autowired
    @Qualifier("testTemplate")
    StringRedisTemplate redisTemplate;

    RedisDeduplicator redisDeduplicator;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
    }

    @BeforeEach
    void setUp() {
        redisDeduplicator = new RedisDeduplicator(redisTemplate, new DeduplicatorRedisApplicationProperties(30000));
    }

    @AfterEach
    void tearDown() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushAll();
    }

    @Test
    void checkAndSet_set() {
        var status = redisDeduplicator.checkAndSet(new DeduplicationKey("key1", String.valueOf(1)));

        assertTrue(status);
    }

    @Test
    void checkAndSet_setWhenExists() {
        var status = redisDeduplicator.checkAndSet(new DeduplicationKey("key1", String.valueOf(1)));
        assertTrue(status);

        // set duplicated member
        status = redisDeduplicator.checkAndSet(new DeduplicationKey("key1", String.valueOf(1)));
        assertFalse(status);
    }

    @Test
    void isUnique() {
        var deduplicationKey = new DeduplicationKey("key1", String.valueOf(1));
        redisDeduplicator.set(deduplicationKey);

        assertFalse(redisDeduplicator.isUnique(deduplicationKey));
    }

    @Test
    void isUnique_notExists() {
        assertTrue(redisDeduplicator.isUnique(new DeduplicationKey("key1", String.valueOf(1))));
    }

    @Test
    void set() {
        var deduplicationKey = new DeduplicationKey("key1", String.valueOf(1));
        assertDoesNotThrow(() -> redisDeduplicator.set(deduplicationKey));
        assertDoesNotThrow(() -> redisDeduplicator.set(deduplicationKey));
    }

    @TestConfiguration
    @Import(value = {RedisConfiguration.class})
    static class LocalTestConfiguration {

        @Primary
        @Bean("testTemplate")
        public StringRedisTemplate stringredisTemplate(RedisConnectionFactory redisConnectionFactory) {
            StringRedisTemplate template = new StringRedisTemplate();
            template.setConnectionFactory(redisConnectionFactory);
            return template;
        }
    }
}