package com.echo.engine.rpc.selector;

/**
 * 服务选择器
 */
public interface ServerSelector<P> {


    ServerInfo select(ServerCluster serverCluster, P param);


}
