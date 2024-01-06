package org.kpi.dedup.redis;

import static java.lang.Boolean.TRUE;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.kpi.dedup.DeduplicationKey;
import org.kpi.dedup.Deduplicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class RedisDeduplicator implements Deduplicator<DeduplicationKey> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisDeduplicator.class);

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
        Long addedMembersAmount = redisTemplate.execute(LuaScripts.SET_IF_NOT_EXISTS_SCRIPT, List.of(key.partition()), String.valueOf(expireAfter(expireDuration)), key.key());
        Objects.requireNonNull(addedMembersAmount);
        putOrAccess(key);

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
        return Objects.isNull(redisTemplate.execute(LuaScripts.MEMBER_SCORE_SCRIPT, List.of(key.partition()), key.key()));
    }

    @Override
    public void set(DeduplicationKey key) {
        Long addedMembersAmount = redisTemplate.execute(LuaScripts.SET_SCRIPT, List.of(key.partition()), String.valueOf(expireAfter(expireDuration)), key.key());
        Objects.requireNonNull(addedMembersAmount);
        putOrAccess(key);

        if (addedMembersAmount == 1 || addedMembersAmount == 0) {
            LOGGER.info("Successfully added key: {}", key);
        } else {
            throw new IllegalStateException("Expected 1 or 0 members to be added!");
        }
    }

    @Override
    public void expireDuplicatedKeys() {
        List<String> partitions = new ArrayList<>(partitionsCache.asMap().keySet());
        if (!partitions.isEmpty()) {
            String currentTimestamp = String.valueOf(clock.millis());
            Long expiredAmount = redisTemplate.execute(LuaScripts.EXPIRE_MEMBERS_SCRIPT, partitions, currentTimestamp);
            partitionsCache.invalidateAll();
            LOGGER.info("Successfully expired {} keys for partitions: {}", expiredAmount, partitions);
        }
    }

    private void putOrAccess(DeduplicationKey key) {
        partitionsCache.get(key.partition(), ignored -> TRUE);
    }

    private long expireAfter(Duration duration) {
        return clock.millis() + duration.toMillis();
    }
}