package com.echo.ioc.condition;

import com.echo.ioc.core.ConfigurableBeanFactory;
import com.echo.ioc.loader.BeanDefinitionRegistry;

/**
 * {@link Condition}条件上下文
 */
public class ConditionContext {


    private final ConfigurableBeanFactory beanFactory;

    private final BeanDefinitionRegistry registry;


    public ConditionContext(BeanDefinitionRegistry registry) {
        this.registry = registry;
        if (registry instanceof ConfigurableBeanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) registry;
        } else {
            this.beanFactory = null;
        }
    }

    public ConfigurableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    public BeanDefinitionRegistry getRegistry() {
        return registry;
    }
}
