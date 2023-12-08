package com.echo.ramcache.core;

/**
 * 内存缓存
 */
public interface RamCache {

    /**
     * 查询缓存名称
     *
     * @return 缓存名称
     */
    String getCacheName();


    /**
     * 移除缓存
     *
     * @param key 缓存key
     */
    void remove(String key);


    /**
     * 更新缓存
     *
     * @param key     缓存key
     * @param content 缓存内容
     */
    void put(String key, Object content);

    /**
     * 查询缓存
     *
     * @param key    缓存key
     * @param tClass /
     * @return 缓存内容 or null
     */
    <T> T get(String key, Class<T> tClass);

    /**
     * 清空缓存
     */
    void clear();


    /**
     * 获取缓存统计数据
     *
     * @return 缓存统计数据
     */
    CacheStat getCacheStat();

}
