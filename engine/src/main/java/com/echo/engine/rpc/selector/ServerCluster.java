package com.echo.engine.rpc.selector;


import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务集群
 *
 * @author: li-yuanwen
 */
public class ServerCluster {

    /**
     * 服务类型
     **/
    private final byte type;
    /**
     * 集群内服务信息
     **/
    private final Map<Integer, ServerInfo> serverInfos = new ConcurrentHashMap<>(16);

    public ServerCluster(byte type) {
        this.type = type;
    }

    public void addServerInfo(ServerInfo serverInfo) {
        serverInfos.put(serverInfo.getId(), serverInfo);
    }


    public byte getType() {
        return type;
    }

    public ServerInfo findMinOnlineNumServerInfo() {
        if (serverInfos.isEmpty()) {
            return null;
        }
        return serverInfos.values().stream().min(Comparator.comparingInt(ServerInfo::getOnlineNum)).get();
    }

    public ServerInfo findByHash(Object param) {
        if (serverInfos.isEmpty()) {
            return null;
        }
        int size = serverInfos.size();
        int index = param.hashCode() % size;
        int i = 0;
        for (ServerInfo serverInfo : serverInfos.values()) {
            if (i++ != index) {
                continue;
            }
            return serverInfo;
        }
        return null;
    }

    public ServerInfo findById(int id) {
        return serverInfos.get(id);
    }
}
