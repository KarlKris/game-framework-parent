package com.echo.ramcache.core;

import lombok.extern.slf4j.Slf4j;

/**
 * 实现基本的数据统计的缓存基类
 *
 * @author li-yuanwen
 */
@Slf4j
public abstract class AbstractCache implements RamCache {

    /**
     * 缓存名称
     **/
    protected final String cacheName;
    /**
     * 缓存数据统计
     **/
    protected final CacheStat cacheStat;

    AbstractCache(String cacheName) {
        this.cacheName = cacheName;
        this.cacheStat = new CacheStat();
    }

    @Override
    public String getCacheName() {
        return this.cacheName;
    }

    @Override
    public CacheStat getCacheStat() {
        return this.cacheStat;
    }

    @Override
    public <T> T get(String key, Class<T> tClass) {
        incrementQuery();
        T value = get0(key, tClass);
        if (value != null) {
            incrementHit();
        }
        return value;
    }

    /**
     * 实际获取缓存值,留给子类实现
     *
     * @param key 缓存key
     * @return value
     */
    protected abstract <T> T get0(String key, Class<T> tClass);

    private void incrementQuery() {
        this.cacheStat.incrementQuery();
    }

    private void incrementHit() {
        this.cacheStat.incrementHit();
    }


}
