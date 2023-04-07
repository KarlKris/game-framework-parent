package com.li.ioc.loader;


import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Field;

/**
 * 单例Bean定义内容
 */
public class BeanDefinition {

    /** beanName **/
    private String beanName;
    /** 类型 **/
    private Class<?> beanClz;


    public String getBeanName() {
        return beanName;
    }

    public Class<?> getBeanClz() {
        return beanClz;
    }

    public Field[] getFields() {
        return ReflectUtil.getFields(beanClz);
    }

}
