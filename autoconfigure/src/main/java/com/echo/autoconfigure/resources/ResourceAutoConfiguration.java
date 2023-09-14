package com.echo.autoconfigure.resources;

import com.echo.common.convert.core.ConfigurableConversionService;
import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.support.DefaultConversionService;
import com.echo.common.convert.support.JsonToObjConverter;
import com.echo.common.convert.support.ObjToJsonConverter;
import com.echo.common.resource.reader.XlsxReader;
import com.echo.common.resource.reader.XmlReader;
import com.echo.common.resource.storage.StorageManager;
import com.echo.ioc.anno.Bean;
import com.echo.ioc.anno.ConditionalOnMissingBean;
import com.echo.ioc.anno.ConditionalOnProperty;
import com.echo.ioc.anno.Configuration;

/**
 * 资源表注入 自动配置
 */
@Configuration
@ConditionalOnProperty({"resource.root.path"})
public class ResourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean("com.echo.common.convert.core.ConfigurableConversionService")
    public ConfigurableConversionService conversionService() {
        ConfigurableConversionService conversionService = new DefaultConversionService();
        conversionService.addConverterFactory(new JsonToObjConverter());
        conversionService.addConverter(new ObjToJsonConverter());
        return conversionService;
    }

    @Bean
    public XmlReader xmlReader(ConversionService conversionService) {
        return new XmlReader(conversionService);
    }

    @Bean
    public XlsxReader xlsxReader(ConversionService conversionService) {
        return new XlsxReader(conversionService);
    }

    @Bean
    public StorageManager storageManager() {
        return new StorageManager();
    }

    @Bean
    public ResourceScanBeanPostFactory resourceScanBeanPostFactory() {
        return new ResourceScanBeanPostFactory();
    }

    @Bean
    public ResourceInjectProcessor resourceInjectProcessor(StorageManager storageManager, ConversionService conversionService) {
        return new ResourceInjectProcessor(storageManager, conversionService);
    }

    @Bean(isLazyInit = false)
    @ConditionalOnProperty(value = "resource.autoReload", havingValue = "true")
    public ResourceAutoReload resourceAutoReload(StorageManager storageManager
            , ResourceScanBeanPostFactory resourceScanBeanPostFactory) {
        return new ResourceAutoReload(resourceScanBeanPostFactory.getPath(), storageManager);
    }

}
