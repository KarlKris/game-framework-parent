package com.li.config;

import com.li.ioc.anno.Component;
import com.li.ioc.anno.Value;

/**
 * @Value注解测试
 */
@Component
public class IocValue {

    @Value("com.li.test")
    private String test;

    @Value("com.li.test2:test2")
    private String test2;

    public String getTest() {
        return test;
    }

    public String getTest2() {
        return test2;
    }

    @Override
    public String toString() {
        return "IocValue{" +
                "test='" + test + '\'' +
                ", test2='" + test2 + '\'' +
                '}';
    }

}
