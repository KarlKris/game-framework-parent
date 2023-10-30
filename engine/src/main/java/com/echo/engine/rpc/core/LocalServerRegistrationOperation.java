package com.echo.engine.rpc.core;

import com.echo.engine.rpc.selector.ServerInfo;

/**
 * 服务注册
 */
public interface LocalServerRegistrationOperation {

    void register(ServerInfo serverInfo);


}
