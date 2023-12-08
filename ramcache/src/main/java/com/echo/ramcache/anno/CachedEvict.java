package com.echo.ramcache.anno;


import java.lang.annotation.*;

/**
 * 缓存移除
 *
 * @author li-yuanwen
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CachedEvict {


    /**
     * 缓存名称 支持SpEl表达式
     **/
    String name();

    /**
     * key值(支持SpEl表达式)
     **/
    String key();

}
