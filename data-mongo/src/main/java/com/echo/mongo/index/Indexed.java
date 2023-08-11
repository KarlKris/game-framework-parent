package com.echo.mongo.index;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注索引
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Indexed {
}
