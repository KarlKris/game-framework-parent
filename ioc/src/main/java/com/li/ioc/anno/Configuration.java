package com.li.ioc.anno;

import java.lang.annotation.*;

/**
 * bean 扫描该类下的所有@Bean注解的方法
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Configuration {
}
