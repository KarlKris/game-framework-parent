package com.echo.ioc.loader;

import com.echo.ioc.anno.ConfigurationProperties;
import com.echo.ioc.util.AnnotatedTypeMetadata;

/**
 * {@link com.echo.ioc.anno.ConfigurationProperties}Properties类BeanDefinition
 *
 * @author: li-yuanwen
 */
public class PropertiesBeanDefinition extends BeanDefinition {

    public PropertiesBeanDefinition(String beanName, Class<?> beanClz, AnnotatedTypeMetadata metadata) {
        super(beanName, beanClz, metadata);
    }

    public String getPropertyPrefixName() {
        ConfigurationProperties annotation = getAnnotation(ConfigurationProperties.class);
        return annotation.prefix();
    }
}
