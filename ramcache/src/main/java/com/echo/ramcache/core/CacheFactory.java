package com.echo.ramcache.core;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存工厂
 *
 * @author: li-yuanwen
 */
public class CacheFactory {

    private final static ConcurrentHashMap<String, RamCache> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends RamCache> T computeIfAbsent(String cacheName, CacheBuilder<T> builder) {
        RamCache ramCache = getCache(cacheName);
        if (ramCache == null) {
            return (T) cache.computeIfAbsent(cacheName, builder::createCache);
        }
        return (T) ramCache;
    }


    @SuppressWarnings("unchecked")
    public static <T extends RamCache> T getCache(String cacheName) {
        return (T) cache.get(cacheName);
    }


}
