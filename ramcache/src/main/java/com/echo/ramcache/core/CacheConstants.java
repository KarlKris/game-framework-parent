package com.echo.ramcache.core;

/**
 * @author li-yuanwen
 * 缓存常量
 */
public interface CacheConstants {

    // 缓存大小常量

    /**
     * 默认大小
     **/
    short DEFAULT_MAXIMUM = 1000;

    /**
     * double
     **/
    short DOUBLE_DEFAULT_MAXIMUM = 2000;


    // 缓存失效时间常量

    /**
     * 5分钟
     **/
    short DEFAULT_EXPIRE_SECOND = 300;

    /**
     * 10分钟
     **/
    short DOUBLE_DEFAULT_EXPIRE_SECOND = 600;

}
