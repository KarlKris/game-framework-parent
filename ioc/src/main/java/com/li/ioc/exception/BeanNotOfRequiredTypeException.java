package com.li.ioc.exception;

/**
 * bean 类型异常
 */
public class BeanNotOfRequiredTypeException extends RuntimeException {

    /** The name of the instance that was of the wrong type. */
    private final String beanName;

    /** The required type. */
    private final Class<?> requiredType;

    /** The offending type. */
    private final Class<?> actualType;


    public BeanNotOfRequiredTypeException(String beanName, Class<?> requiredType, Class<?> actualType) {
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
