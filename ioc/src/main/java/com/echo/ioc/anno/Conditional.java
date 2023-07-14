package com.echo.ioc.anno;

import com.echo.ioc.condition.Condition;

import java.lang.annotation.*;

/**
 * 条件注入注解
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conditional {


    Class<? extends Condition>[] value();

}
