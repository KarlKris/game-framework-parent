package com.echo.ioc.exception;

/**
 * 获取不到Bean异常
 */
public class NoSuchBeanException extends BeansException {

    public NoSuchBeanException(String message) {
        super(message);
    }

    public NoSuchBeanException(String message, Throwable cause) {
        super(message, cause);
    }

}
