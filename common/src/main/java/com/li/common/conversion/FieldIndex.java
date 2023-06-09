package com.li.common.conversion;

import java.lang.annotation.*;

/**
 * 属性字段顺序注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldIndex {

    /** 字段顺序,序列化 **/
    int order();

}
