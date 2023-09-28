package com.echo.network.protocol;

/**
 * 用@InBody注解装饰的参数
 *
 * @author li-yuanwen
 * @date 2021/7/31 15:13
 **/
public class InBodyMethodParameter extends AbstractMethodParameter {

    public InBodyMethodParameter(Class<?> clazz) {
        super(clazz);
    }


}
