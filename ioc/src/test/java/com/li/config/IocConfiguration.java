package com.li.config;

import com.li.bean.IocComponentC;
import com.li.ioc.anno.Bean;
import com.li.ioc.anno.Configuration;
import com.li.ioc.processor.ConfigurationPropertiesBeanFactoryPostProcessor;

/**
 * @Configuration注解bean
 */
@Configuration
public class IocConfiguration {

    @Bean
    public ConfigurationPropertiesBeanFactoryPostProcessor processor() {
        return new ConfigurationPropertiesBeanFactoryPostProcessor("application.properties");
    }

    @Bean
    public IocComponentC iocComponentC() {
        return new IocComponentC();
    }

    @Bean("iocComponentC2")
    public IocComponentC iocComponentC2() {
        return new IocComponentC();
    }
}
