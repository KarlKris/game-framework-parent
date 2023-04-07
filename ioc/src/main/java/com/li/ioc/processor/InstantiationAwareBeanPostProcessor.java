package com.li.ioc.processor;

/**
 * Bean实例化后置处理器
 */
public interface InstantiationAwareBeanPostProcessor {


    /**
     * bean实例化后置处理
     * @param bean bean
     * @param beanName beanName
     * @return true表处理成功,false表跳过,会跳过剩余的InstantiationAwareBeanPostProcessor处理器
     */
    default boolean postProcessAfterInstantiation(Object bean, String beanName) {
        return true;
    }

}
