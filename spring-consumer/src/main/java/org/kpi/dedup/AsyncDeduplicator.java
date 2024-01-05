package org.kpi.dedup;

import java.util.concurrent.CompletableFuture;

public interface AsyncDeduplicator<T> {

    CompletableFuture<Boolean> checkAndSet(T key);

    CompletableFuture<Boolean> isUnique(T key);

    CompletableFuture<Boolean> set(T key);

}