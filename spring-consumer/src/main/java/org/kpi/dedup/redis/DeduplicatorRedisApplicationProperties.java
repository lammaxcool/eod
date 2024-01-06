package org.kpi.dedup.redis;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("application.redis")
public record DeduplicatorRedisApplicationProperties(
        @Positive long expireDurationMillis
) {
}