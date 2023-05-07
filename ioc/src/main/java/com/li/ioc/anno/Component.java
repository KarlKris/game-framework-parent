package com.li.ioc.anno;

import java.lang.annotation.*;

/**
 * 单例对象注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Component {

    /**
     * @return 指定beanName
     */
    String value() default "";

}
