package com.li.ioc.loader;


import cn.hutool.core.util.ReflectUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 单例Bean定义内容
 */
public class BeanDefinition {

    /** beanName **/
    private final String beanName;
    /** 类型 **/
    private final Class<?> beanClz;
    /** 创建Bean 工厂beanName **/
    private final String factoryBeanName;
    /** 创建Bean 工厂method **/
    private final Method factoryMethod;

    public BeanDefinition(String beanName, Class<?> beanClz) {
        this(beanName, beanClz, null, null);
    }

    public BeanDefinition(String beanName, Class<?> beanClz, String factoryBeanName, Method factoryMethod) {
        this.beanName = beanName;
        this.beanClz = beanClz;
        this.factoryBeanName = factoryBeanName;
        this.factoryMethod = factoryMethod;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getBeanClz() {
        return beanClz;
    }

    public Field[] getFields() {
        return ReflectUtil.getFields(beanClz);
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public Method getFactoryMethod() {
        return factoryMethod;
    }

}
