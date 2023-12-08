package com.echo.engine.rpc.core;

import com.echo.engine.client.Address;
import com.echo.engine.rpc.selector.ServerInfo;

public interface ServerUploadOperation {

    /**
     * 注册服务
     *
     * @param address    注册进程地址
     * @param serverInfo 服务信息
     */
    void upload(Address address, ServerInfo serverInfo);

}
