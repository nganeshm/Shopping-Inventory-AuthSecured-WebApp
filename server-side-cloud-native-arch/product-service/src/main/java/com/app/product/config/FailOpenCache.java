package com.app.product.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.lang.Nullable;

import java.util.concurrent.Callable;

/**
 * Decorator that treats Redis as optional.
 *
 * <p>A cache miss and a Redis outage are the same from the caller's point of
 * view: load from PostgreSQL. Writes that cannot reach Redis are logged and
 * dropped so a catalog GET never becomes a 500.</p>
 *
 * <p>{@code @Cacheable(sync = true)} uses {@link #get(Object, Callable)}, which
 * acquires a Redis lock. That path does not go through Spring's
 * {@code CacheErrorHandler}, which is why "Unable to connect to Redis" was
 * escaping to the API. This decorator closes that gap at the Cache contract.</p>
 */
@Slf4j
final class FailOpenCache implements Cache {

    private final Cache delegate;

    FailOpenCache(Cache delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    @Nullable
    public ValueWrapper get(Object key) {
        try {
            return delegate.get(key);
        } catch (RuntimeException ex) {
            log.warn("Redis GET failed for cache={} key={}; using database", getName(), key);
            return null;
        }
    }

    @Override
    @Nullable
    public <T> T get(Object key, @Nullable Class<T> type) {
        try {
            return delegate.get(key, type);
        } catch (RuntimeException ex) {
            log.warn("Redis GET failed for cache={} key={}; using database", getName(), key);
            return null;
        }
    }

    @Override
    @Nullable
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper cached = get(key);
        if (cached != null) {
            @SuppressWarnings("unchecked")
            T value = (T) cached.get();
            return value;
        }
        try {
            return valueLoader.call();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ValueRetrievalException(key, valueLoader, ex);
        }
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        try {
            delegate.put(key, value);
        } catch (RuntimeException ex) {
            log.warn("Redis PUT failed for cache={} key={}; response will not be cached", getName(), key);
        }
    }

    @Override
    @Nullable
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        try {
            return delegate.putIfAbsent(key, value);
        } catch (RuntimeException ex) {
            log.warn("Redis PUT failed for cache={} key={}; response will not be cached", getName(), key);
            return null;
        }
    }

    @Override
    public void evict(Object key) {
        try {
            delegate.evict(key);
        } catch (RuntimeException ex) {
            log.warn("Redis EVICT failed for cache={} key={}", getName(), key);
        }
    }

    @Override
    public void clear() {
        try {
            delegate.clear();
        } catch (RuntimeException ex) {
            log.warn("Redis CLEAR failed for cache={}", getName());
        }
    }
}
