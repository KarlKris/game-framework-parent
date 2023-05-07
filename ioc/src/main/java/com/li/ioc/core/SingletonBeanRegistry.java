package com.li.ioc.core;

/**
 * 单例注册
 */
public interface SingletonBeanRegistry {

    /**
     * 注册单例
     * @param beanName beanName
     * @param bean bean
     */
    void addSingleton(String beanName, Object bean);


    /**
     * 获取单例对象
     * @param beanName beanName
     * @return bean
     */
    Object getSingleton(String beanName);

}
