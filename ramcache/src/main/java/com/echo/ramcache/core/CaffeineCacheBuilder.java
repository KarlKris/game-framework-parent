package com.echo.ramcache.core;

/**
 * CaffeineCache 构建
 *
 * @author: li-yuanwen
 */
public class CaffeineCacheBuilder implements CacheBuilder<CaffeineCache> {

    /**
     * 缓存最大容量
     **/
    private final long maximum;
    /**
     * 过期时间(秒)
     **/
    private final long expire;

    public CaffeineCacheBuilder(long maximum, long expire) {
        this.maximum = maximum;
        this.expire = expire;
    }

    @Override
    public CaffeineCache createCache(String cacheName) {
        return new CaffeineCache(cacheName, maximum, expire);
    }
}
