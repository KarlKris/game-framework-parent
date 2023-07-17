package com.echo.resources;

import com.echo.ioc.context.AnnotationGenericApplicationContext;
import com.echo.ioc.processor.ConfigurationPropertiesBeanFactoryPostProcessor;
import org.junit.Test;

/**
 * @author: li-yuanwen
 */
public class ResourcesTest {


    @Test
    public void resourceAutoConfigure() {
        AnnotationGenericApplicationContext context = new AnnotationGenericApplicationContext("com.echo");
        context.addBeanFactoryPostProcessor(new ConfigurationPropertiesBeanFactoryPostProcessor());
        context.refresh();

        TestResourceInject bean = context.getBean(TestResourceInject.class);
        bean.print();


        try {
            Thread.sleep(1 * 60 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


}
