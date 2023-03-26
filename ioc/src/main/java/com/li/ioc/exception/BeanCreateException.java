package com.li.ioc.exception;

/**
 * Bean单例创建异常
 * @author li-yuanwen
 * @date 2023/3/25
 */
public class BeanCreateException extends RuntimeException {

    public BeanCreateException(String message) {
        super(message);
    }

    public BeanCreateException(String message, Throwable cause) {
        super(message, cause);
    }
}
