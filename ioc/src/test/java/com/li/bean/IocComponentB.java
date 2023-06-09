package com.li.bean;

import com.li.ioc.anno.Autowired;
import com.li.ioc.anno.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * ioc测试beanB
 */
@Slf4j
@Component
public class IocComponentB {

    @Autowired
    private IocComponentA componentA;

    public IocComponentB() {
        log.info("IocComponentB init");
    }
}
