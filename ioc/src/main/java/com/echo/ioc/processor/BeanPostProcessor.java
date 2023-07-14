package com.echo.ioc.processor;

import com.echo.ioc.exception.BeansException;

/**
 * 为Bean实例做自定义的修改的接口
 */
public interface BeanPostProcessor {


    /**
     * Bean 初始化前修改接口
     * @param bean bean实例
     * @param beanName beanName
     * @return bean or null
     * @throws BeansException
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * Bean 初始化后修改接口
     * @param bean bean实例
     * @param beanName beanName
     * @return bean or null
     * @throws BeansException
     */
    default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
