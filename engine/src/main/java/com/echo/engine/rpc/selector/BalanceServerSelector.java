package com.echo.engine.rpc.selector;

/**
 * 基于在线人数负载选择最少人数的服务器
 *
 * @author: li-yuanwen
 */
public class BalanceServerSelector implements ServerSelector<Object> {

    @Override
    public ServerInfo select(ServerCluster serverCluster, Object param) {
        ServerInfo serverInfo = serverCluster.findMinOnlineNumServerInfo();
        if (serverInfo == null) {
            throw new RuntimeException("no server type: " + serverCluster.getType());
        }
        return serverInfo;
    }
}
