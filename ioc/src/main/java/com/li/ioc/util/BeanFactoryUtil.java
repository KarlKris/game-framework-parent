package com.li.ioc.util;

import com.li.common.util.StringUtil;

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
        return StringUtil.lowerFirst(clz.getSimpleName());
    }

}
