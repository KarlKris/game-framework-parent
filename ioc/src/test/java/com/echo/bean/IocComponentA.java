package com.echo.bean;

import com.echo.ioc.anno.Autowired;
import com.echo.ioc.anno.Component;
import com.echo.ioc.anno.Qualifier;
import com.echo.ioc.context.ApplicationContext;
import com.echo.ioc.processor.BeanFactoryPostProcessor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * ioc测试beanA
 */
@Component
public class IocComponentA {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private IocComponentB componentB;
    @Autowired
    @Qualifier("iocComponentC")
    private IocComponentC iocComponentC;


    @PostConstruct
    public void init() {
        BeanFactoryPostProcessor bean = applicationContext.getBean(BeanFactoryPostProcessor.class);
        System.out.println(bean);
    }

    @PreDestroy
    public void close() {
        System.out.println(this + " dead");
    }


}
