package com.li.ioc.core;

/**
 * Bean 工厂
 */
public interface BeanFactory {

    /**
     * 从容器中获取指定类型的单例对象
     * @param clz 类型Class
     * @return 对象
     * @param <T> 类型
     */
    <T> T getBean(Class<T> clz);

    /**
     * 从容器中获取指定类型,指定beanName的单例对象
     * @param beanName beanName
     * @param requiredType 类型Class
     * @return 对象
     * @param <T> 类型
     */
    <T> T getBean(String beanName, Class<T> requiredType);

    /**
     * 销毁容器中的所有单例
     */
    void destroy();

}
