package com.echo.engine.rpc.core;

import com.echo.engine.boostrap.NettyServerBootstrap;
import com.echo.engine.client.Address;
import com.echo.engine.client.GenericNettyClient;
import com.echo.engine.client.NettyClient;
import com.echo.engine.handler.ClientBusinessHandler;
import com.echo.engine.handler.NettyClientChannelInitializer;
import com.echo.engine.rpc.selector.SelectType;
import com.echo.engine.rpc.selector.ServerCluster;
import com.echo.engine.rpc.selector.ServerInfo;

import javax.net.ssl.SSLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务发现
 *
 * @author: li-yuanwen
 */
public class GenericRemoteLocalServerSeekOperation implements RemoteServerSeekOperation, LocalServerRegistrationOperation {

    /**
     * 服务发现
     **/
    private final Map<Byte, ServerCluster> cluster = new ConcurrentHashMap<>(4);
    /**
     * 进程连接
     **/
    private final Map<Address, NettyClient> clients = new ConcurrentHashMap<>();

    /**
     * NettyServerBootstrap
     **/
    private final NettyServerBootstrap bootstrap;
    /**
     * NettyClientChannelInitializer
     **/
    private final NettyClientChannelInitializer initializer;

    public GenericRemoteLocalServerSeekOperation(NettyServerBootstrap bootstrap) throws SSLException {
        this.bootstrap = bootstrap;
        this.initializer = new NettyClientChannelInitializer(bootstrap.getSettings()
                , bootstrap.getMessageEncoder()
                , new ClientBusinessHandler(bootstrap.getProtocolContext(), bootstrap.getPushOperation()
                , bootstrap.getInvocationContext()
                , bootstrap.getBusinessLoopGroup())
                , bootstrap.getHeartBeatHandler());
    }

    @Override
    public NettyClient seekApplication(byte type, long identity) {
        ServerCluster serverCluster = seekServerCluster(type);
        return select(serverCluster, SelectType.IDENTITY_SELECTOR, identity);
    }

    @Override
    public NettyClient seekApplication(byte type, int serverId) {
        ServerCluster serverCluster = seekServerCluster(type);
        return select(serverCluster, SelectType.SPECIFIC_SELECTOR, serverId);
    }

    @Override
    public NettyClient seekApplication(byte type, SelectType selectType) {
        return seekApplication(type, selectType, null);
    }

    @Override
    public NettyClient seekApplication(byte type, SelectType selectType, Object selectParam) {
        ServerCluster serverCluster = seekServerCluster(type);
        return select(serverCluster, selectType, selectParam);
    }

    private ServerCluster seekServerCluster(byte type) {
        ServerCluster serverCluster = cluster.get(type);
        if (serverCluster == null) {
            throw new RuntimeException("not found server type: " + type);
        }
        return serverCluster;
    }

    private NettyClient select(ServerCluster serverCluster, SelectType selectType, Object param) {
        ServerInfo serverInfo = selectType.getSelector().select(serverCluster, param);
        if (serverInfo == null) {
            throw new RuntimeException("not found server, selectType: " + selectType.name() + " selectParam:" + param);
        }
        return clients.computeIfAbsent(serverInfo.getAddress(), this::newGenericNettyClient);
    }

    private GenericNettyClient newGenericNettyClient(Address address) {
        return new GenericNettyClient(address, bootstrap.getClientEventLoopGroup(), initializer
                , bootstrap.getInvocationContext(), bootstrap.getProtocolContext(), bootstrap.getMessageFactory());
    }

    // -----------------------------------------------------------------------------------------------------------------


    @Override
    public void register(ServerInfo serverInfo) {
        ServerCluster serverCluster = cluster.computeIfAbsent(serverInfo.getType()
                , k -> new ServerCluster(serverInfo.getType()));
        serverCluster.addServerInfo(serverInfo);
    }
}
