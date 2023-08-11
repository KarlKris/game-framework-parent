package com.echo.mongo.excetion;

/**
 * @author: li-yuanwen
 */
public abstract class DataAccessException extends RuntimeException {


    /**
     * Constructor for DataAccessException.
     * @param msg the detail message
     */
    public DataAccessException(String msg) {
        super(msg);
    }

    /**
     * Constructor for DataAccessException.
     * @param msg the detail message
     * @param cause the root cause (usually from using an underlying
     * data access API such as JDBC)
     */
    public DataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
