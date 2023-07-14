package com.echo.ioc.condition;

import com.echo.ioc.util.AnnotatedTypeMetadata;

/**
 * bean 条件判断
 */
@FunctionalInterface
public interface Condition {


    boolean match(ConditionContext context, AnnotatedTypeMetadata metadata);

}
