package com.echo.ioc.exception;

import com.echo.common.util.ClassUtils;

/**
 * bean 类型异常
 */
public class BeanNotOfRequiredTypeException extends BeansException {

    /** The name of the instance that was of the wrong type. */
    private final String beanName;

    /** The required type. */
    private final Class<?> requiredType;

    /** The offending type. */
    private final Class<?> actualType;


    public BeanNotOfRequiredTypeException(String beanName, Class<?> requiredType, Class<?> actualType) {
        super("Bean named '" + beanName + "' is expected to be of type '" + ClassUtils.getQualifiedName(requiredType) +
                "' but was actually of type '" + ClassUtils.getQualifiedName(actualType) + "'");
        this.beanName = beanName;
        this.requiredType = requiredType;
        this.actualType = actualType;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getRequiredType() {
        return requiredType;
    }

    public Class<?> getActualType() {
        return actualType;
    }
}
