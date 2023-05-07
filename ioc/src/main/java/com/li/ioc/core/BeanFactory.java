package com.li.ioc.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Bean 工厂
 */
public interface BeanFactory {

    /**
     * 从容器中获取指定BeanName的单例对象
     * @param beanName beanName
     * @return 对象
     */
    Object getBean(String beanName);

    /**
     * 从容器中获取指定类型的单例对象
     * @param requiredType 类型Class
     * @return 对象
     * @param <T> 类型
     */
    <T> T getBean(Class<T> requiredType);

    /**
     * 从容器中获取指定类型,指定beanName的单例对象
     * @param beanName beanName
     * @param requiredType 类型Class
     * @return 对象
     * @param <T> 类型
     */
    <T> T getBean(String beanName, Class<T> requiredType);

    /**
     * 获取容器内所有的requiredType类型的单例beanName,包含子类
     * @param requiredType 类型
     * @return beanName集
     */
    List<String> getBeanNamesByType(Class<?> requiredType);

    /**
     * 获取容器内所有的requiredType类型的单例bean,包含子类
     * @param requiredType 类型
     * @return 单例集 key:beanName value:bean
     * @param <T> /
     */
    <T> Map<String, T> getBeansByType(Class<T> requiredType);


}
