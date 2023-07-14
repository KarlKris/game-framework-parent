package com.echo.ioc.processor;

import com.echo.ioc.context.ApplicationContext;
import com.echo.ioc.context.ApplicationContextAware;
import com.echo.ioc.exception.BeansException;

/**
 * ApplicationContextAware.setApplicationContext()注入
 */
public class ApplicationContextAwareProcessor implements BeanPostProcessor {

    private final ApplicationContext applicationContext;

    public ApplicationContextAwareProcessor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof ApplicationContextAware)) {
            return bean;
        }
        ApplicationContextAware aware = (ApplicationContextAware) bean;
        aware.setApplicationContext(applicationContext);
        return bean;
    }
}
