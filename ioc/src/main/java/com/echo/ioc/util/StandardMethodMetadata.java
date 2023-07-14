package com.echo.ioc.util;

import cn.hutool.core.annotation.AnnotationUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 以Method为基础 获取方法上注解信息
 */
public class StandardMethodMetadata implements AnnotatedTypeMetadata {

    private final Method method;

    public StandardMethodMetadata(Method method) {
        this.method = method;
    }

    public String getDeclaringClassName() {
        return method.getDeclaringClass().getName();
    }

    public String getMethodName() {
        return method.getName();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return AnnotationUtil.getAnnotation(method, annotationType);
    }
}
