package org.kpi.dedup;

public record DeduplicationKey(String key, String partition) {
}