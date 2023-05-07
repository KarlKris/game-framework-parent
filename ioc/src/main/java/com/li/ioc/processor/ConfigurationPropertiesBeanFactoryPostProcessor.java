package com.li.ioc.processor;

import cn.hutool.core.io.resource.ClassPathResource;
import com.li.ioc.core.ConfigurableBeanFactory;
import com.li.ioc.prop.MapPropertySource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 配置文件配置数据 添加进容器内
 */
public class ConfigurationPropertiesBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    /** 配置文件名称 **/
    private static final String DEFAULT_CONFIG_FILE_NAME = "application.properties";

    /** 文件名 **/
    private final String fileName;

    public ConfigurationPropertiesBeanFactoryPostProcessor(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableBeanFactory beanFactory) {
        ClassPathResource classPathResource = new ClassPathResource(fileName);
        Properties properties = new Properties();
        try {
            properties.load(classPathResource.getStream());
            Map<String, String> map = new HashMap<>(properties.size());
            for (String propertyName : properties.stringPropertyNames()) {
                map.put(propertyName, properties.getProperty(propertyName));
            }
            beanFactory.addPropertySource(new MapPropertySource(fileName, map));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
