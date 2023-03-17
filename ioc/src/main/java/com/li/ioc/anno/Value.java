package com.li.ioc.anno;

import java.lang.annotation.*;

/**
 * 解析注入
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {

    /** 解析式 **/
    String value();

    /** 是否必须存在 **/
    boolean required() default true;

}
