package com.echo.network.exception;

import lombok.Getter;

/**
 * @author li-yuanwen
 * @date 2021/7/31 15:49
 * 错误请求异常
 **/
@Getter
public class BadRequestException extends SocketException {

    public BadRequestException(int errorCode) {
        super(errorCode);
    }

    public BadRequestException(int errorCode, String message) {
        super(errorCode, message);
    }


}
