package com.echo.mongo.excetion;

/**
 * 非法使用api异常
 * @author: li-yuanwen
 */
public class InvalidMongoDbApiUsageException extends DataAccessException {

    public InvalidMongoDbApiUsageException(String msg) {
        super(msg);
    }

    public InvalidMongoDbApiUsageException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
