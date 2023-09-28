package com.echo.network.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 对外方法协议注解
 * @author li-yuanwen
 * @date 2021/7/30 21:00
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SocketMethod {

    /** 协议号 **/
    byte id();

    /**
     * 是否是同步协议
     * 对于一些需要rpc的协议,线程可以不必同步等待rpc返回,而是rpc返回时再返回协议返回内容
     * 即可以使用CompletableFuture.whenComplete()返回协议内容,减少不必要的等待
     * @return true 同步协议
     */
    boolean isSyncMethod() default true;


}
