package com.echo.engine.rpc.register;

import com.echo.engine.rpc.selector.ServerInfo;

/**
 * 服务注册
 *
 * @author: li-yuanwen
 */
public interface RegisterOperation {


    void register(String ip, int port, ServerInfo serverInfo);


}
