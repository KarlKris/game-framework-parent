package com.echo;

import com.echo.bean.IocComponentA;
import com.echo.bean.IocComponentB;
import com.echo.config.IocConfiguration;
import com.echo.config.IocValue;
import com.echo.ioc.context.AnnotationGenericApplicationContext;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Ioc测试入口
 */
public class IocTest {


    @Test
    public void ioc() throws InterruptedException {
        AnnotationGenericApplicationContext context = new AnnotationGenericApplicationContext("com.echo");
        context.refresh();

        int num = 10;
        final CountDownLatch latch = new CountDownLatch(num);
        for (int i = 0; i < num; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    IocComponentB componentB = context.getBean(IocComponentB.class);
                    IocComponentA componentA = context.getBean(IocComponentA.class);
                    IocConfiguration iocConfiguration = context.getBean(IocConfiguration.class);
                    IocValue iocValue = context.getBean(IocValue.class);

                    System.out.println(componentA);
                    System.out.println(componentB);
                    System.out.println(iocConfiguration);
                    System.out.println(iocValue);
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        context.close();
    }

}
