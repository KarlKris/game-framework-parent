package com.echo.engine.rpc.selector;

import com.echo.common.id.MultiServerIdGenerator;

/**
 * 基于标识选择服务器,例如根据分布式唯一id的机器标识来选择服务器
 *
 * @author: li-yuanwen
 */
public class IdentityServerSelector implements ServerSelector<Long> {

    @Override
    public ServerInfo select(ServerCluster serverCluster, Long param) {
        int workerId = MultiServerIdGenerator.toWorkerId(param);
        ServerInfo serverInfo = serverCluster.findById(workerId);
        if (serverInfo == null) {
            throw new RuntimeException("can't find server => type:" + serverCluster.getType() + " workerId: " + workerId);
        }
        return serverInfo;
    }
}
