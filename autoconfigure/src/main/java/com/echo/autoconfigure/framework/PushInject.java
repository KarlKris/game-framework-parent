package com.echo.autoconfigure.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 推送代理注入注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PushInject {

    /**
     * OuterMessage消息
     *
     * @return 推送到外网
     */
    boolean outerMessage() default true;


}
