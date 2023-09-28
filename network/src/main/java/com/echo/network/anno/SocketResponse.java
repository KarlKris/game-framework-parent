package com.echo.network.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 协议返回对象注解
 * @author li-yuanwen
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SocketResponse {

    /** 业务模块号 **/
    short module();

    /** 协议号 **/
    byte id();

}
