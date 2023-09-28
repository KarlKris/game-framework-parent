package com.echo.network.protocol;

/**
 * @author li-yuanwen
 * @date 2021/7/31 15:02
 * 方法参数接口
 **/
public interface MethodParameter {


    /**
     * 获取参数类型
     *
     * @return 参数类型
     */
    Class<?> getParameterClass();

}
