package com.echo.engine.rpc.selector;

/**
 * 哈希选择服务器
 *
 * @author: li-yuanwen
 */
public class HashServerSelector implements ServerSelector<Object> {

    @Override
    public ServerInfo select(ServerCluster serverCluster, Object param) {
        ServerInfo serverInfo = serverCluster.findByHash(param);
        if (serverInfo == null) {
            throw new RuntimeException("no server type: " + serverCluster.getType());
        }
        return serverInfo;
    }
}
