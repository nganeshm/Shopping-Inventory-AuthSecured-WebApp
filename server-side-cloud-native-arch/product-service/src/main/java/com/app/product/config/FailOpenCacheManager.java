package com.app.product.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;

import java.util.Collection;

/**
 * Wraps every named cache so Redis outages stay inside the cache layer.
 * Services keep depending on {@link CacheManager}, not Redis APIs.
 */
final class FailOpenCacheManager implements CacheManager {

    private final CacheManager delegate;

    FailOpenCacheManager(CacheManager delegate) {
        this.delegate = delegate;
    }

    @Override
    @Nullable
    public Cache getCache(String name) {
        Cache cache = delegate.getCache(name);
        return cache == null ? null : new FailOpenCache(cache);
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }
}
