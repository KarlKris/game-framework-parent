package com.echo.network.modules;


import com.echo.network.message.SocketProtocol;

/**
 * 错误码模块
 *
 * @author li-yuanwen
 * @date 2021/12/13
 */
public interface ErrorCodeModule {

    SocketProtocol ERROR_CODE_RESPONSE = new SocketProtocol(ErrorCodeModule.MODULE, ErrorCodeModule.ERROR_CODE);

    /**
     * 模块号
     **/
    short MODULE = 1;

    /**
     * 错误码
     **/
    byte ERROR_CODE = 1;

}
