package com.echo.autoconfigure.framework.gateway;

import com.echo.autoconfigure.framework.NettyProperties;
import com.echo.autoconfigure.framework.NettyPropertiesSettingsBuilderCustomizer;
import com.echo.autoconfigure.framework.ProtocolInvocationRegister;
import com.echo.autoconfigure.framework.ProtocolScanner;
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
 * MMO快捷搭建网关服 自动注入
 *
 * @author: li-yuanwen
 */
@Configuration
@ConditionalOnProperty(value = "netty.server.type", havingValue = "GATEWAY")
@EnableConfigurationProperties(NettyProperties.class)
public class GatewayAutoConfiguration {


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

    private GatewayBusinessHandler businessHandler(NettyServerBootstrap bootstrap) {
        return new GatewayBusinessHandler(bootstrap.getProtocolContext()
                , bootstrap.getSessionContext(), new GatewayDispatcher(bootstrap));
    }

}
