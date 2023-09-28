package com.echo.network.exception;

import lombok.Getter;

/**
 * 服务器逻辑异常基础类
 *
 * @author li-yuanwen
 * @date 2021/8/4 22:24
 **/
@Getter
public class SocketException extends RuntimeException {

    /**
     * 错误码
     **/
    private final int errorCode;

    public SocketException(int errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public SocketException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SocketException(int errorCode, String message, Throwable e) {
        super(message, e);
        this.errorCode = errorCode;
    }
}
