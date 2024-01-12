package org.kpi.dedup.redis;

import org.intellij.lang.annotations.Language;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public final class LuaScripts {

    @Language("LUA")
    private static final String SET_IF_NOT_EXISTS_SCRIPT_VALUE = """
            local set_name = KEYS[1]   -- The name of the sorted set
            local timestamp = ARGV[1]  -- The timestamp to be used as the score
            local member = ARGV[2]     -- The member to add to the set
                        
            -- Add member to the sorted set wih the timestamp as the score
            local added = redis.call('ZADD', set_name, 'NX', timestamp, member)
                        
            if added > 0 then
             local latest_timestamp = redis.call('ZREVRANGE', set_name, 0, 0, 'WITHSCORES')[2]
                if latest_timestamp then
                    -- Calculate the TTL as the difference between the latest timestamp and the current time
                    local ttl = tonumber(latest_timestamp) - redis.call('TIME')[1]
                    if ttl > 0 then
                        -- Set the TTL for the key
                        redis.call('EXPIRE', set_name, ttl, 'GT')
                    end
                end
            end
                        
            -- Return whether a new member was
            return added;
            """;
    static final RedisScript<Long> SET_IF_NOT_EXISTS_SCRIPT = new DefaultRedisScript<>(SET_IF_NOT_EXISTS_SCRIPT_VALUE, Long.class);

    @Language("LUA")
    private static final String SET_SCRIPT_VALUE = """
            local set_name = KEYS[1]   -- The name of the sorted set
            local timestamp = ARGV[1]  -- The timestamp to be used as the score
            local member = ARGV[2]     -- The member to add to the set
                        
            -- Add member to the sorted set wih the timestamp as the score
            return redis.call('ZADD', set_name, timestamp, member)
            """;
    static final RedisScript<Long> SET_SCRIPT = new DefaultRedisScript<>(SET_SCRIPT_VALUE, Long.class);

    @Language("LUA")
    private static final String MEMBER_SCORE_SCRIPT_VALUE = """
            local set_name = KEYS[1]   -- The name of the sorted set
            local member = ARGV[1]     -- The member to check
                        
            -- Add member to the sorted set wih the timestamp as the score
            return redis.call('ZSCORE', set_name, member)
            """;
    static final RedisScript<Long> MEMBER_SCORE_SCRIPT = new DefaultRedisScript<>(MEMBER_SCORE_SCRIPT_VALUE, Long.class);

    @Language("LUA")
    private static final String EXPIRE_MEMBERS_SCRIPT_VALUE = """
            local set_name = KEYS[1]   -- The name of the sorted set
            local threshold = ARGV[1]  -- The timestamp to be used as the score
                        
            -- Expire members with score less than timestamp
            return redis.call('ZREMRANGEBYSCORE', set_name, '-inf', threshold)
            """;
    static final RedisScript<Long> EXPIRE_MEMBERS_SCRIPT = new DefaultRedisScript<>(EXPIRE_MEMBERS_SCRIPT_VALUE, Long.class);

    @Language("LUA")
    private static final String REMOVE_MEMBER_SCRIPT_VALUE = """
            local set_name = KEYS[1]   -- The name of the sorted set
            local member = ARGV[1]  -- The member to remove
                        
            -- Remove specified members
            return redis.call('ZREM', set_name, member)
            """;
    static final RedisScript<Long> REMOVE_MEMBER_SCRIPT = new DefaultRedisScript<>(REMOVE_MEMBER_SCRIPT_VALUE, Long.class);

    private LuaScripts() {
        throw new UnsupportedOperationException();
    }
}