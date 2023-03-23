package com.li.ioc.loader;

/**
 * BeanDefinition加载器
 */
public interface BeanDefinitionLoader {

    /**
     * 加载BeanDefinition
     * @return 加载数量
     */
    int loadBeanDefinitions();

}
