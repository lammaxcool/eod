package org.kpi.dedup;

public interface Deduplicator<T> {

    boolean checkAndSet(T key);

    boolean isUnique(T key);

    void set(T key);

}