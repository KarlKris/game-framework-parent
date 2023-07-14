package com.echo.condition;

import com.echo.ioc.context.AnnotationGenericApplicationContext;
import org.junit.Test;

/**
 * {@link com.echo.ioc.anno.Conditional} 相关类测试
 */
public class ConditionalTest {


    @Test
    public void conditional() {
        AnnotationGenericApplicationContext context = new AnnotationGenericApplicationContext("com.echo");
        context.refresh();

        System.out.println("finish");
    }

}
