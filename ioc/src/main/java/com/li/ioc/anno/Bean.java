package com.li.ioc.anno;

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


}
