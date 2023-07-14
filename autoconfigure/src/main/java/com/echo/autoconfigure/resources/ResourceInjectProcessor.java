package com.echo.autoconfigure.resources;

import com.echo.ioc.exception.BeansException;
import com.echo.ioc.processor.InstantiationAwareBeanPostProcessor;

/**
 * {@link ResourceInject} 注解注入实现
 */
public class ResourceInjectProcessor implements InstantiationAwareBeanPostProcessor {


    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        // todo
        return true;
    }
}
