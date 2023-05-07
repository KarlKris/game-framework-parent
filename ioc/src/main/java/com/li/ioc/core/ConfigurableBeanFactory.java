package com.li.ioc.core;


import com.li.ioc.prop.PropertySource;
import com.li.ioc.prop.PropertySources;

/**
 * 提供配置BeanFactory的各种方法
 */
public interface ConfigurableBeanFactory extends BeanFactory, SingletonBeanRegistry {

    /** #@Value 注解默认值 分隔符 **/
    String DEFAULT_VALUE_SEPARATOR = ":";

    /**
     * 往容器添加配置属性集
     * @param propertySources 配置属性集
     */
    void addPropertySources(PropertySources propertySources);

    /**
     * 往容器添加配置属性
     * @param propertySource 属性值
     */
    void addPropertySource(PropertySource<?> propertySource);

    /**
     * 销毁容器中的所有单例
     */
    void destroy();

}
