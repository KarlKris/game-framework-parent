package com.echo.bean;

import com.echo.ioc.anno.Autowired;
import com.echo.ioc.anno.Component;
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
