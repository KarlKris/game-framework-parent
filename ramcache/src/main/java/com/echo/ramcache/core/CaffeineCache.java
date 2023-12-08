package com.echo.ramcache.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 基于Caffeine的缓存
 *
 * @author li-yuanwen
 */
@Slf4j
public class CaffeineCache extends AbstractCache {

    /**
     * 缓存
     **/
    private final Cache<String, Object> cache;


    public CaffeineCache(String cacheName, long maximum, long expire) {
        super(cacheName);
        this.cache = Caffeine.newBuilder()
                .maximumSize(maximum)
                .expireAfterAccess(expire, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void remove(String key) {
        if (log.isDebugEnabled()) {
            log.debug("remove from Caffeine [{}],key[{}]", getCacheName(), key);
        }
        this.cache.invalidate(key);
    }

    @Override
    public void put(String key, Object content) {
        if (log.isDebugEnabled()) {
            log.debug("add Caffeine [{}], key[{}]", getCacheName(), key);
        }
        this.cache.put(key, content);
    }

    @Override
    protected <T> T get0(String key, Class<T> tClass) {
        if (log.isDebugEnabled()) {
            log.debug("get Caffeine[{}], key[{}]", getCacheName(), key);
        }
        return (T) (this.cache.getIfPresent(key));
    }

    @Override
    public void clear() {
        this.cache.invalidateAll();
    }

}
