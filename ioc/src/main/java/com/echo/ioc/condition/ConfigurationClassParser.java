package com.echo.ioc.condition;

import com.echo.ioc.anno.Bean;
import com.echo.ioc.loader.BeanDefinition;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 解析 {@link com.echo.ioc.anno.Configuration} 修饰的类
 */
public class ConfigurationClassParser {



    public void parse(List<BeanDefinition> configurationBeanDefinitions) {
        for (BeanDefinition definition : configurationBeanDefinitions) {
            for (Method method : definition.getBeanClz().getDeclaredMethods()) {
                if (method.getAnnotation(Bean.class) != null) {
                    definition.addBeanMethod(method);
                }
            }
        }
    }

}
