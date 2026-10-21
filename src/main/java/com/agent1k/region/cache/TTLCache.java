package com.agent1k.region.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TTLCache<K, V> {

    private final Map<K, Entry<V>> storage = new ConcurrentHashMap<>();
    private final long ttlMillis;

    public TTLCache(long ttlSeconds) {
        this.ttlMillis = Math.max(1L, ttlSeconds) * 1000L;
    }

    public void put(@NotNull K key, @NotNull V value) {
        storage.put(key, new Entry<>(value, System.currentTimeMillis() + ttlMillis));
    }

    @Nullable
    public V get(@NotNull K key) {
        Entry<V> entry = storage.get(key);
        if (entry == null) return null;
        if (entry.expiresAt() < System.currentTimeMillis()) {
            storage.remove(key);
            return null;
        }
        return entry.value();
    }

    public void invalidate(@NotNull K key) {
        storage.remove(key);
    }

    public void clear() {
        storage.clear();
    }

    private record Entry<V>(@NotNull V value, long expiresAt) {}
}
