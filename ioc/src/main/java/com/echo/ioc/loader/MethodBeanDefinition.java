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
    /** 创建bean 时机 **/
    private final boolean lazyInit;

    public MethodBeanDefinition(String beanName, Class<?> beanClz, String factoryBeanName, Method factoryMethod, boolean isLazyInit) {
        super(beanName, beanClz, new StandardMethodMetadata(factoryMethod));
        this.factoryBeanName = factoryBeanName;
        this.factoryMethod = factoryMethod;
        this.lazyInit = isLazyInit;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public Method getFactoryMethod() {
        return factoryMethod;
    }

    @Override
    public boolean isLazyInit() {
        return lazyInit;
    }
}
