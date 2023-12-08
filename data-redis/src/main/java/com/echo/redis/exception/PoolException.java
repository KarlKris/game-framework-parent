package com.echo.redis.exception;

import com.echo.common.exception.NestedRuntimeException;

/**
 * @author: li-yuanwen
 */
public class PoolException extends NestedRuntimeException {

    /**
     * Constructs a new <code>PoolException</code> instance.
     *
     * @param msg the detail message.
     */
    public PoolException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new <code>PoolException</code> instance.
     *
     * @param msg   the detail message.
     * @param cause the nested exception.
     */
    public PoolException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
