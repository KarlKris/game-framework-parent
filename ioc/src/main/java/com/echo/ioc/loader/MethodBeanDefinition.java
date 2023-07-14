package com.echo.ioc.loader;

import com.echo.ioc.util.StandardMethodMetadata;

import java.lang.reflect.Method;

/**
 * 基于{@link com.echo.ioc.anno.Bean} 注入的BeanDefinition
 */
public class MethodBeanDefinition extends BeanDefinition {

    /** 创建Bean 工厂beanName **/
    private final String factoryBeanName;
    /** 创建Bean 工厂method **/
    private final Method factoryMethod;

    public MethodBeanDefinition(String beanName, Class<?> beanClz, String factoryBeanName, Method factoryMethod) {
        super(beanName, beanClz, new StandardMethodMetadata(factoryMethod));
        this.factoryBeanName = factoryBeanName;
        this.factoryMethod = factoryMethod;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public Method getFactoryMethod() {
        return factoryMethod;
    }

}
