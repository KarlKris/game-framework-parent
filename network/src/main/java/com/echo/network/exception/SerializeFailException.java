package com.echo.network.exception;


/**
 * @author li-yuanwen
 * @date 2021/7/31 18:50
 * 序列化或反序列化失败异常
 **/
public class SerializeFailException extends RuntimeException {


    public SerializeFailException(String message, Throwable cause) {
        super(message, cause);
    }

}
