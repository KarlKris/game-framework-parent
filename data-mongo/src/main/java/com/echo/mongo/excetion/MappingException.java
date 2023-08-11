package com.echo.mongo.excetion;

/**
 * mongo 映射异常
 * @author: li-yuanwen
 */
public class MappingException extends RuntimeException {

    public MappingException(
            String s) {
        super(s);
    }

    public MappingException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
