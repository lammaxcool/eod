package org.kpi.dedup.redis;

import static java.lang.Boolean.TRUE;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.intellij.lang.annotations.Language;
import org.kpi.dedup.Deduplicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class RedisDeduplicator implements Deduplicator<DeduplicationKey> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisDeduplicator.class);

    private static final Duration DEFAULT_EXPIRE_DURATION = Duration.ofMinutes(5);

    @Language("LUA")
    private static final String SET_IF_NOT_EXISTS_SCRIPT_VALUE = """
            local set_name = KEYS[1]   -- The name of the sorted set
            local timestamp = ARGV[1]  -- The timestamp to be used as the score
            local member = ARGV[2]     -- The member to add to the set
                        
            -- Add member to the sorted set wih the timestamp as the score
            return redis.call('ZADD', set_name, 'NX', timestamp, member)
            """;
    private static final RedisScript<Long> SET_IF_NOT_EXISTS_SCRIPT = new DefaultRedisScript<>(SET_IF_NOT_EXISTS_SCRIPT_VALUE, Long.class);

    @Language("LUA")
    private static final String SET_SCRIPT_VALUE = """
            local set_name = KEYS[1]   -- The name of the sorted set
            local timestamp = ARGV[1]  -- The timestamp to be used as the score
            local member = ARGV[2]     -- The member to add to the set
                        
            -- Add member to the sorted set wih the timestamp as the score
            return redis.call('ZADD', set_name, timestamp, member)
            """;
    private static final RedisScript<Long> SET_SCRIPT = new DefaultRedisScript<>(SET_SCRIPT_VALUE, Long.class);

    @Language("LUA")
    private static final String MEMBER_SCORE_SCRIPT_VALUE = """
            local set_name = KEYS[1]   -- The name of the sorted set
            local member = ARGV[1]     -- The member to check
                        
            -- Add member to the sorted set wih the timestamp as the score
            return redis.call('ZSCORE', set_name, member)
            """;
    private static final RedisScript<Long> MEMBER_SCORE_SCRIPT = new DefaultRedisScript<>(MEMBER_SCORE_SCRIPT_VALUE, Long.class);

    @Language("LUA")
    private static final String EXPIRE_MEMBERS_SCRIPT_VALUE = """
            local set_name = KEYS[1]   -- The name of the sorted set
            local threshold = ARGV[1]  -- The timestamp to be used as the score
                        
            -- Expire members with score less than timestamp
            return redis.call('ZREMRANGEBYSCORE', set_name, '-inf', threshold)
            """;
    private static final RedisScript<Long> EXPIRE_MEMBERS_SCRIPT = new DefaultRedisScript<>(EXPIRE_MEMBERS_SCRIPT_VALUE, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final Duration expireDuration;

    private final Cache<String, Boolean> partitionsCache;
    private final Clock clock = Clock.systemUTC();

    public RedisDeduplicator(StringRedisTemplate redisTemplate, DeduplicatorRedisApplicationProperties properties) {
        this.redisTemplate = redisTemplate;
        this.expireDuration = Duration.ofMillis(properties.expireDurationMillis());

        this.partitionsCache = Caffeine.newBuilder()
                .expireAfterAccess(expireDuration)
                .build();
    }

    @Override
    public boolean checkAndSet(DeduplicationKey key) {
        partitionsCache.put(key.partition(), TRUE);
        Long addedMembersAmount = redisTemplate.execute(SET_IF_NOT_EXISTS_SCRIPT, List.of(key.partition()), String.valueOf(expireDuration.toMillis()), key.key());
        Objects.requireNonNull(addedMembersAmount);

        if (addedMembersAmount == 1) {
            LOGGER.info("Successfully added key: {}", key);
            return true;
        } else if (addedMembersAmount == 0) {
            LOGGER.warn("Duplicated key: {}", key);
            return false;
        } else {
            throw new IllegalStateException("Expected 1 or 0 members to be added!");
        }
    }

    @Override
    public boolean isUnique(DeduplicationKey key) {
        return Objects.isNull(redisTemplate.execute(MEMBER_SCORE_SCRIPT, List.of(key.partition()), key.key()));
    }

    @Override
    public void set(DeduplicationKey key) {
        partitionsCache.put(key.partition(), TRUE);
        Long addedMembersAmount = redisTemplate.execute(SET_SCRIPT, List.of(key.partition()), String.valueOf(expireDuration.toMillis()), key.key());
        Objects.requireNonNull(addedMembersAmount);

        if (addedMembersAmount == 1 || addedMembersAmount == 0) {
            LOGGER.info("Successfully added key: {}", key);
        } else {
            throw new IllegalStateException("Expected 1 or 0 members to be added!");
        }
    }

    @Scheduled(initialDelayString = "${application.redis.expire-duration-millis:30000}", fixedRateString = "${application.redis.expire-duration-millis:30000}")
    private void expireMembers() {
        List<String> partitions = new ArrayList<>(partitionsCache.asMap().keySet());
        String currentTimestamp = String.valueOf(clock.instant().toEpochMilli());
        Long expiredAmount = redisTemplate.execute(SET_SCRIPT, partitions, currentTimestamp);
        LOGGER.info("Successfully expired {} keys from partitions: {}", expiredAmount, partitions);
    }
}