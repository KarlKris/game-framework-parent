package com.echo.network.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记业务入口
 * @author li-yuanwen
 * @date 2021/7/30 20:59
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SocketController {

    /**
     * 业务模块号
     **/
    short module();

    /**
     * 业务逻辑所处的服务器类型
     **/
    byte serverType() default 0;
}
