package com.echo.ioc.anno;

import java.lang.annotation.*;

/**
 * 修饰方法,将方法返回值作为单例对象
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {

    /**
     * @return 指定beanName
     */
    String value() default "";


    /**
     * @return true 按需初始化bean;false 容器加载成功后初始化
     */
    boolean isLazyInit() default true;

}
