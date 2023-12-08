package com.echo.ramcache.anno;


import com.echo.ramcache.core.CacheConstants;

import java.lang.annotation.*;

/**
 * 用于某个方法，每次被调用，此方法都执行，并把结果更新到PutCache配置的地方，一般用于缓存更新
 *
 * @author li-yuanwen
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CachedPut {

    /**
     * 缓存名称 支持SpEl表达式
     *
     * @return 缓存名称
     */
    String name();

    /**
     * 缓存内容标识
     *
     * @return 缓存内容标识
     */
    String key();

    /**
     * 缓存大小
     **/
    short maximum() default CacheConstants.DEFAULT_MAXIMUM;

    /**
     * 失效时间(秒)
     **/
    short expire() default CacheConstants.DOUBLE_DEFAULT_EXPIRE_SECOND;

}
