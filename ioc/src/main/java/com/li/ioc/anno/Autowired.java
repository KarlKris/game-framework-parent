package com.li.ioc.anno;

import java.lang.annotation.*;

/**
 * 注入bean注解,用于构造函数参数或field
 * @author li-yuanwen
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {

    /**
     * @return 注入的bean实例是否必须存在 默认是true
     */
    boolean required() default true;

}
