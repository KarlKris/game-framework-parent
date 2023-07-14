package com.echo.ioc.util;

import java.lang.annotation.Annotation;

/**
 * 附带 注解信息的 元数据
 */
public interface AnnotatedTypeMetadata {



    default boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return getAnnotation(annotationType) != null;
    }


    <A extends Annotation> A getAnnotation(Class<A> annotationType);

}
