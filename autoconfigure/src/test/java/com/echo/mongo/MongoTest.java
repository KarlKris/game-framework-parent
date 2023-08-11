package com.echo.mongo;

import com.echo.ioc.context.AnnotationGenericApplicationContext;
import com.echo.ioc.processor.ConfigurationPropertiesBeanFactoryPostProcessor;
import org.junit.Test;

/**
 * @author: li-yuanwen
 */
public class MongoTest {

    @Test
    public void mongoTest() {
        AnnotationGenericApplicationContext context = new AnnotationGenericApplicationContext("com.echo");
        context.addBeanFactoryPostProcessor(new ConfigurationPropertiesBeanFactoryPostProcessor());
        context.refresh();

        com.echo.mongo.entity.Test test = new com.echo.mongo.entity.Test();
        test.id = 1;
        test.name = "test1";

    }

}
