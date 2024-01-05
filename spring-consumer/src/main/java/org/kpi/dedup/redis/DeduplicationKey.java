package org.kpi.dedup.redis;

public record DeduplicationKey(String key, String partition) {
}