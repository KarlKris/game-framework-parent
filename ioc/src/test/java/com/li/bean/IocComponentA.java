package com.li.bean;

import com.li.ioc.anno.Autowired;
import com.li.ioc.anno.Component;
import com.li.ioc.anno.Qualifier;
import com.li.ioc.context.ApplicationContext;
import com.li.ioc.processor.BeanFactoryPostProcessor;

import javax.annotation.PostConstruct;

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

}
