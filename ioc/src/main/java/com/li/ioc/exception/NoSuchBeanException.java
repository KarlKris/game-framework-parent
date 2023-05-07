package com.li.ioc.exception;

/**
 * 获取不到Bean异常
 */
public class NoSuchBeanException extends RuntimeException {



    public NoSuchBeanException(String message) {
        super(message);
    }

    public NoSuchBeanException(String message, Throwable cause) {
        super(message, cause);
    }

}
