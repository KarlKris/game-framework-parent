package com.echo.ioc.core;

import lombok.NonNull;

/**
 * 工厂Bean
 */
public interface FactoryBean {


    /**
     * 获取工厂构建的bean
     * @param context bean创建上下文
     * @return bean
     */
    @NonNull
    Object getObject(BeanCreateContext context);

    /**
     * bean class对象
     * @return /
     */
    @NonNull
    Class<?> getObjectClass();

}
