package com.echo.ioc.loader;

import com.echo.ioc.exception.NoSuchBeanDefinitionException;

import java.util.Collection;

/**
 * BeanDefinition注册器
 */
public interface BeanDefinitionRegistry {

    /** 注册BeanDefinition集 **/
    void registerBeanDefinitions(Collection<BeanDefinition> beanDefinitions);

    /** 注册BeanDefinition **/
    void registerBeanDefinition(BeanDefinition beanDefinition);

    /**
     * 判断注册器内是否存在对应BeanDefinition
     * @param beanName beanName
     * @return true 存在
     */
    boolean containsBeanDefinition(String beanName);

    /**
     * 删除注册器内对应的BeanDefinition
     * @param beanName beanName
     */
    void removeBeanDefinition(String beanName);

    /**
     * 获取注册器内所有的BeanNames
     * @return BeanName 数组
     */
    String[] getBeanDefinitionNames();

    /** 根据bean获取BeanDefinition **/
    BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

}
