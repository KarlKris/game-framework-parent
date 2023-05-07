package com.li.bean;

import com.li.ioc.anno.Autowired;
import com.li.ioc.anno.Component;

/**
 * ioc测试beanB
 */
@Component
public class IocComponentB {

    @Autowired
    private IocComponentA componentA;

}
