package com.echo.autoconfigure.resources;

import com.echo.ioc.anno.Bean;
import com.echo.ioc.anno.ConditionalOnProperty;
import com.echo.ioc.anno.Configuration;

/**
 * 资源表注入 自动配置
 */
@Configuration
@ConditionalOnProperty({"resource.root.path"})
public class ResourceAutoConfiguration {

    @Bean
    public ResourceScanBeanPostFactory resourceScanBeanPostFactory() {
        return new ResourceScanBeanPostFactory();
    }


    @Bean
    public ResourceInjectProcessor resourceInjectProcessor() {
        return new ResourceInjectProcessor();
    }

}
