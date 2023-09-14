package com.echo.ioc.anno;

import java.lang.annotation.*;

/**
 * 标识配置文件类
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperties {

    /**
     * @return 配置文件key前缀
     */
    String prefix() default "";

}
