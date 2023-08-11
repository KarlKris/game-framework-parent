package com.echo.common.convert;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Annotation to clarify intended usage of a Converter as reading converter
 * in case the conversion types leave room for disambiguation.
 */
@Target(TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadingConverter {
}
