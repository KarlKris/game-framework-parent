package com.echo.ioc.util;

import cn.hutool.core.annotation.AnnotationUtil;

import java.lang.annotation.Annotation;

/**
 * 以Class为基础 获取类上注解信息
 */
public class StandardClassMetadata implements AnnotatedTypeMetadata {

    private final Class<?> clz;

    public StandardClassMetadata(Class<?> clz) {
        this.clz = clz;
    }

    public String getClassName() {
        return clz.getName();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return AnnotationUtil.getAnnotation(clz, annotationType);
    }
}
