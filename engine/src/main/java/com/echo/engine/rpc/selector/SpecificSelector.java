package com.echo.engine.rpc.selector;

/**
 * 指定机器标识来选择服务器
 *
 * @author: li-yuanwen
 */
public class SpecificSelector implements ServerSelector<Integer> {

    @Override
    public ServerInfo select(ServerCluster serverCluster, Integer param) {
        ServerInfo serverInfo = serverCluster.findById(param);
        if (serverInfo == null) {
            throw new RuntimeException("can't find server => type:" + serverCluster.getType() + " workerId: " + param);
        }
        return serverInfo;
    }
}
