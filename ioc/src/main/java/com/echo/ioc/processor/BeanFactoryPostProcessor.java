package com.echo.ioc.processor;

import com.echo.ioc.core.ConfigurableBeanFactory;

/**
 * beanFactory容器后置处理器
 */
public interface BeanFactoryPostProcessor extends Ordered {


    /**
     * 对beanFactory容器进行后置处理
     * @param beanFactory 容器
     */
    void postProcessBeanFactory(ConfigurableBeanFactory beanFactory);

}
