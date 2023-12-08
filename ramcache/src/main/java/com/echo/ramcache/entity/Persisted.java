package com.echo.ramcache.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Persisted {


    /**
     * 持久化间隔
     *
     * @return 持久化间隔, 默认5分钟
     */
    int intervalSecond() default 300;


}
