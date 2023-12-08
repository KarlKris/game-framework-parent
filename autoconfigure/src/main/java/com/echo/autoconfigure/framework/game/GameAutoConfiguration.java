package com.echo.autoconfigure.framework.game;

import com.echo.autoconfigure.framework.*;
import com.echo.engine.boostrap.NettyServerBootstrap;
import com.echo.engine.config.NettyServerSettings;
import com.echo.engine.handler.NettyServerChannelInitializer;
import com.echo.engine.rpc.core.RemoteServerSeekOperation;
import com.echo.ioc.anno.Bean;
import com.echo.ioc.anno.ConditionalOnProperty;
import com.echo.ioc.anno.Configuration;
import com.echo.ioc.anno.EnableConfigurationProperties;

import javax.net.ssl.SSLException;

/**
 * MMO快捷搭建游戏服 自动注入
 *
 * @author: li-yuanwen
 */
@Configuration
@ConditionalOnProperty(value = "netty.server.type", havingValue = "1")
@EnableConfigurationProperties(NettyProperties.class)
public class GameAutoConfiguration {

    @Bean
    public NettyServerBootstrap nettyServerBootstrap(NettyProperties nettyProperties) throws SSLException {
        NettyServerSettings.NettyServerSettingsBuilder builder = NettyServerSettings.builder();
        new NettyPropertiesSettingsBuilderCustomizer(nettyProperties).customize(builder);
        NettyServerBootstrap bootstrap = new NettyServerBootstrap(builder.build());
        NettyServerChannelInitializer initializer = new NettyServerChannelInitializer(bootstrap, businessHandler(bootstrap));
        bootstrap.setInitializer(initializer);
        ProtocolScanner.scan(nettyProperties.getProtocolPackage(), bootstrap.getProtocolContext());
        return bootstrap;
    }

    @Bean
    public RemoteServerSeekOperation remoteServerSeekOperation(NettyServerBootstrap bootstrap) {
        return bootstrap.getRemoteServerSeekOperation();
    }

    @Bean
    public ProtocolInvocationRegister protocolInvocationRegister(NettyServerBootstrap bootstrap) {
        return new ProtocolInvocationRegister(bootstrap.getProtocolContext());
    }

    @Bean
    public NettyShutdown nettyShutdown(NettyServerBootstrap bootstrap) {
        return new NettyShutdown(bootstrap);
    }

    private GameBusinessHandler businessHandler(NettyServerBootstrap bootstrap) {
        return new GameBusinessHandler(bootstrap.getProtocolContext()
                , bootstrap.getSessionContext(), new GameDispatcher(bootstrap));
    }

}
