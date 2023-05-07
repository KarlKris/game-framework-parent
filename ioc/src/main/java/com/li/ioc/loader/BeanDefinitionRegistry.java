package com.li.ioc.loader;

import com.li.ioc.exception.NoSuchBeanDefinitionException;

import java.util.Collection;

/**
 * BeanDefinition注册器
 */
public interface BeanDefinitionRegistry {

    /** 注册BeanDefinition集 **/
    void registerBeanDefinitions(Collection<BeanDefinition> beanDefinitions);

    /** 注册BeanDefinition **/
    void registerBeanDefinition(BeanDefinition beanDefinition);

    /** 根据bean获取BeanDefinition **/
    BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

}
