package com.app.product.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Shared Redis cache policy for product-service.
 *
 * <p>PostgreSQL remains the source of truth. Redis contains response DTOs only,
 * never JPA entities, which avoids serializing lazy Hibernate proxies and keeps
 * the cache contract independent of persistence internals.</p>
 *
 * <p>Catalog metadata changes slowly and therefore receives a longer TTL.
 * Product and inventory responses include stock quantities, so they expire
 * quickly and are also evicted after every related write.</p>
 *
 * <p>Redis is optional. The process starts without a live broker, and a later
 * outage is treated as a cache miss. APIs keep serving from PostgreSQL.</p>
 */
@Configuration
@EnableCaching
@Slf4j
public class RedisCacheConfig implements CachingConfigurer {

    private static final String KEY_PREFIX = "secureauth:product:";

    private final RedisConnectionFactory connectionFactory;

    public RedisCacheConfig(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        RedisCacheConfiguration defaults = configuration(Duration.ofMinutes(5));

        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                CacheNames.CATEGORY_BY_ID, configuration(Duration.ofHours(1)),
                CacheNames.CATEGORY_CATALOG, configuration(Duration.ofMinutes(30)),
                CacheNames.PRODUCT_BY_ID, configuration(Duration.ofMinutes(5)),
                CacheNames.PRODUCT_CATALOG, configuration(Duration.ofMinutes(2)),
                CacheNames.INVENTORY_BY_PRODUCT, configuration(Duration.ofSeconds(30))
        );

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
        // Fail-open sits inside the transaction-aware proxy so a Redis outage
        // during after-commit PUT/EVICT is swallowed instead of failing the API.
        return new TransactionAwareCacheManagerProxy(new FailOpenCacheManager(redisCacheManager));
    }

    private RedisCacheConfiguration configuration(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> KEY_PREFIX + cacheName + "::")
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    /**
     * Cache availability must not become API availability. If Redis is down,
     * log the event and continue with PostgreSQL (fail-open cache behavior).
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis GET failed for cache={} key={}; falling back to database",
                        cache.getName(), key, exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Redis PUT failed for cache={} key={}; response will not be cached",
                        cache.getName(), key, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis EVICT failed for cache={} key={}", cache.getName(), key, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Redis CLEAR failed for cache={}", cache.getName(), exception);
            }
        };
    }
}
