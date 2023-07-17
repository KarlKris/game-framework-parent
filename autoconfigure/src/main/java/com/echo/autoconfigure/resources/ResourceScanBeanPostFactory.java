package com.echo.autoconfigure.resources;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.ClassScanner;
import com.echo.common.resource.ResourceDefinition;
import com.echo.common.resource.anno.ResourceObj;
import com.echo.common.resource.storage.StorageManager;
import com.echo.ioc.anno.Value;
import com.echo.ioc.core.ConfigurableBeanFactory;
import com.echo.ioc.processor.BeanFactoryPostProcessor;

/**
 * 资源表扫描bean
 */
public class ResourceScanBeanPostFactory implements BeanFactoryPostProcessor {


    /** 资源表路径 **/
    @Value("resource.root.path")
    private String path;
    /** 资源类包名前缀 **/
    @Value("resource.class.basePackage:com.echo")
    private String basePackage;

    public String getPath() {
        return path;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableBeanFactory beanFactory) {
        StorageManager storageManager = beanFactory.getBean(StorageManager.class);
        for (Class<?> clz : new ClassScanner(basePackage).scan()) {
            ResourceObj resourceObj = AnnotationUtil.getAnnotation(clz, ResourceObj.class);
            if (resourceObj == null) {
                continue;
            }
            ResourceDefinition resourceDefinition = parseResourceDefinition(clz, path);
            if (resourceDefinition == null) {
                continue;
            }
            storageManager.initialize(resourceDefinition, beanFactory.getBean(resourceObj.reader()));
        }

    }

    private ResourceDefinition parseResourceDefinition(Class<?> clz, String rootPath) {
        ResourceObj obj = AnnotationUtil.getAnnotation(clz, ResourceObj.class);
        if (obj == null) {
            return null;
        }
        return new ResourceDefinition(clz, rootPath);
    }
}
