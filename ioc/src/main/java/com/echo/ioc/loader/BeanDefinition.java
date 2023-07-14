package com.echo.ioc.loader;


import cn.hutool.core.util.ReflectUtil;
import com.echo.ioc.anno.Conditional;
import com.echo.ioc.anno.Configuration;
import com.echo.ioc.util.AnnotatedTypeMetadata;
import com.echo.ioc.util.StandardClassMetadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * 单例Bean定义内容
 */
public class BeanDefinition implements AnnotatedBeanDefinition {

    /** beanName **/
    private final String beanName;
    /** 类型 **/
    private final Class<?> beanClz;

    /** beanMethods **/
    private final List<Method> beanMethods = new LinkedList<>();

    /** 注解数据 **/
    private final AnnotatedTypeMetadata metadata;

    public BeanDefinition(String beanName, Class<?> beanClz) {
        this(beanName, beanClz, new StandardClassMetadata(beanClz));
    }

    public BeanDefinition(String beanName, Class<?> beanClz, AnnotatedTypeMetadata metadata) {
        this.beanName = beanName;
        this.beanClz = beanClz;
        this.metadata = metadata;
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

    public List<Method> getBeanMethods() {
        return beanMethods;
    }

    public void addBeanMethod(Method method) {
        this.beanMethods.add(method);
    }

    @Override
    public AnnotatedTypeMetadata getMetadata() {
        return metadata;
    }


    public boolean isConfigurationClass() {
        return metadata.hasAnnotation(Configuration.class) || metadata.hasAnnotation(Conditional.class);
    }


}
