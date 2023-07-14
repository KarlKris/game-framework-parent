package com.echo.config;

import com.echo.bean.IocComponentC;
import com.echo.ioc.anno.Bean;
import com.echo.ioc.anno.Configuration;
import com.echo.ioc.processor.ConfigurationPropertiesBeanFactoryPostProcessor;

/**
 * @Configuration 注解 test
 */
@Configuration
public class IocConfiguration {

    @Bean
    public ConfigurationPropertiesBeanFactoryPostProcessor processor() {
        return new ConfigurationPropertiesBeanFactoryPostProcessor();
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
