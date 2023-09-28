package com.echo.network.modules;


import com.echo.network.message.SocketProtocol;

/**
 * 错误码
 *
 * @author: li-yuanwen
 */
public class ErrorCode {

    /**
     * 请求序号
     **/
    private long reqSn;
    /**
     * 模块号
     **/
    private short module;
    /**
     * 方法标识
     **/
    private byte methodId;
    /**
     * 错误码
     **/
    private int code;

    public ErrorCode(long reqSn, SocketProtocol protocol, int code) {
        this.reqSn = reqSn;
        this.module = protocol.getModule();
        this.methodId = protocol.getMethodId();
        this.code = code;
    }

    public long getReqSn() {
        return reqSn;
    }

    public short getModule() {
        return module;
    }

    public byte getMethodId() {
        return methodId;
    }

    public int getCode() {
        return code;
    }
}
