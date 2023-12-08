package com.echo.ramcache.exception;

/**
 * @author li-yuanwen
 */
public class EnhanceException extends RuntimeException {

    /**
     * 尝试被增强的实体
     **/
    private final Object entity;

    public EnhanceException(Object entity) {
        super();
        this.entity = entity;
    }

    public EnhanceException(Object entity, Throwable cause) {
        super(cause);
        this.entity = entity;
    }
}
