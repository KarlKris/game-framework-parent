package com.echo.common.resource.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 资源外键注解
 * @author li-yuanwen
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceForeignKey {


    /**
     * 外键
     * @return 外键类型
     */
    Class<?> foreignKeyClz();

    /**
     * 外键属性名
     * @return 外键属性名
     */
    String foreignKeyFieldName();

}
