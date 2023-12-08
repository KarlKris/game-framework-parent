package com.echo.common.data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * 标注 不会被持久化的字段
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { FIELD, METHOD, ANNOTATION_TYPE })
public @interface Transient {


}
