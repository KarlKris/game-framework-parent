package com.echo.ramcache.core;

/**
 * 缓存构建器
 */
public interface CacheBuilder<T extends RamCache> {


    /**
     * 创建缓存
     *
     * @param cacheName 缓存名称
     * @return 缓存
     */
    T createCache(String cacheName);


}
