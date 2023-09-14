package com.echo.ioc.anno;

import java.lang.annotation.*;

/**
 * 配合{@link Configuration}和{@link ConfigurationProperties}将配置文件内容注入进bean中
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableConfigurationProperties {

    /**
     * 注册{@link ConfigurationProperties} 修饰的配置类bean
     */
    Class<?>[] value() default {};

}
