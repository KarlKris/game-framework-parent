package com.echo.mongo.excetion;

/**
 * @author: li-yuanwen
 */
public class InvalidDataAccessApiUsageException extends DataAccessException {

    /**
     * Constructor for InvalidDataAccessApiUsageException.
     *
     * @param msg the detail message
     */
    public InvalidDataAccessApiUsageException(String msg) {
        super(msg);
    }

    /**
     * Constructor for InvalidDataAccessApiUsageException.
     *
     * @param msg   the detail message
     * @param cause the root cause from the data access API in use
     */
    public InvalidDataAccessApiUsageException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
