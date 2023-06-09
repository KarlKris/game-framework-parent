package com.li.ioc.util;

import com.li.common.util.StringUtils;

/**
 * BeanFactory 工具类
 */
public class BeanFactoryUtil {


    /**
     * 根据class生成beanName
     * @param clz class
     * @return beanName
     */
    public static String generateBeanName(Class<?> clz) {
        return StringUtils.lowerFirst(clz.getSimpleName());
    }

}
