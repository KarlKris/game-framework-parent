package com.li.ioc.exception;

import java.util.Collection;

/**
 * 通过类型自动装配时,多个相同类型的Bean,且没有指定beanName 异常
 */
public class NoUniqueBeanDefinitionException extends NoSuchBeanDefinitionException {
    private final Collection<String> beanNamesFound;

    public NoUniqueBeanDefinitionException(Class<?> requiredType, Collection<String> beanNamesFound) {
        super("no unique bean: " + requiredType.getName(), requiredType);
        this.beanNamesFound = beanNamesFound;
    }

    public Collection<String> getBeanNamesFound() {
        return beanNamesFound;
    }
}
