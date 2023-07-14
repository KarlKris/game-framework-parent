package com.echo.ioc.processor;


/**
 * 排序接口
 */
public interface Ordered {


    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;


    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;


    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }

}
