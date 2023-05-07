package com.li.ioc.processor;

import com.li.ioc.core.ConfigurableBeanFactory;

/**
 * beanFactory容器后置处理器
 */
public interface BeanFactoryPostProcessor {


    /**
     * 对beanFactory容器进行后置处理
     * @param beanFactory 容器
     */
    void postProcessBeanFactory(ConfigurableBeanFactory beanFactory);

}
