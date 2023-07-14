package com.echo.common.resource.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 资源索引注解,可用于属性上(表单一索引),也可以用于方法上返回String值表复合索引
 * @author li-yuanwen
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceIndex {

    /**
     * 索引名称
     * @return 索引名称
     */
    String indexName();

    /**
     * 是否是唯一索引
     * @return true 唯一索引
     */
    boolean uniqueIndex() default false;

}
