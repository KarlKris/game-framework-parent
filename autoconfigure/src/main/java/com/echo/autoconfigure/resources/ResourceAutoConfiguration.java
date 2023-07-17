package com.echo.autoconfigure.resources;

import com.echo.common.conversion.ConversionService;
import com.echo.common.conversion.ConvertType;
import com.echo.common.conversion.converter.JsonToObjConverter;
import com.echo.common.conversion.converter.ObjToJsonConverter;
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
    @ConditionalOnMissingBean("com.echo.common.conversion.ConversionService")
    public ConversionService conversionService() {
        ConversionService conversionService = new ConversionService();
        conversionService.addConverter(ConvertType.JSON, new JsonToObjConverter());
        conversionService.addConverter(ConvertType.JSON, new ObjToJsonConverter());
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
