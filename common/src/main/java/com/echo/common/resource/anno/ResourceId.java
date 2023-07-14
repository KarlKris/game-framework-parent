package com.echo.common.resource.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 资源表唯一标识注解，可用于属性上,也可以用于方法上返回String值表复合唯一标识
 * @author li-yuanwen
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceId {
}
