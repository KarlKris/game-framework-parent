package com.echo.ioc.processor;

import com.echo.ioc.exception.BeansException;

/**
 * Bean实例化后置处理器
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {


    /**
     * bean实例化前置处理
     * @param beanClass beanClass
     * @param beanName beanName
     * @return return bean表创建bean成功,null表不创建对应的bean
     * @throws BeansException
     */
    default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    /**
     * bean实例化后置处理
     * @param bean bean
     * @param beanName beanName
     * @return true表处理成功,false表跳过,会跳过剩余的InstantiationAwareBeanPostProcessor处理器
     */
    default boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        return true;
    }

}
