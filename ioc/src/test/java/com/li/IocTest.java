package com.li;

import com.li.bean.IocComponentA;
import com.li.bean.IocComponentB;
import com.li.config.IocConfiguration;
import com.li.config.IocValue;
import com.li.ioc.context.AnnotationGenericApplicationContext;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Ioc测试入口
 */
public class IocTest {


    @Test
    public void ioc() throws InterruptedException {
        AnnotationGenericApplicationContext context = new AnnotationGenericApplicationContext("com.li");
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
    }

}
