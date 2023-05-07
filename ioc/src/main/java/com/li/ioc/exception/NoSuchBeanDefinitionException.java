package com.li.ioc.exception;

/**
 * 创建bean时,没有对应的BeanDefinition异常
 */
public class NoSuchBeanDefinitionException extends RuntimeException {

    private String beanName;

    private Class<?> requiredType;

    public NoSuchBeanDefinitionException(String beanName) {
        super("No bean named '" + beanName + "' available");
        this.beanName = beanName;
    }

    public NoSuchBeanDefinitionException(Throwable cause, String beanName) {
        super("No bean named '" + beanName + "' available", cause);
        this.beanName = beanName;
    }

    public NoSuchBeanDefinitionException(String message, Class<?> requiredType) {
        super(message);
        this.requiredType = requiredType;
    }

    public NoSuchBeanDefinitionException(Class<?> requiredType) {
        super("No bean class '" + requiredType.getName() + "' available");
        this.requiredType = requiredType;
    }

    public NoSuchBeanDefinitionException(Throwable cause, Class<?> requiredType) {
        super("No bean class '" + requiredType.getName() + "' available, cause");
        this.requiredType = requiredType;
    }

    public Class<?> getRequiredType() {
        return requiredType;
    }

    public String getBeanName() {
        return beanName;
    }
}
