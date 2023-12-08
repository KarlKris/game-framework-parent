package com.echo.autoconfigure.framework;

import com.echo.autoconfigure.SettingsBuilderCustomizer;
import com.echo.engine.config.NettyServerSettings;

/**
 * @author: li-yuanwen
 */
public class NettyPropertiesSettingsBuilderCustomizer implements SettingsBuilderCustomizer<NettyServerSettings.NettyServerSettingsBuilder> {

    private final NettyProperties nettyProperties;

    public NettyPropertiesSettingsBuilderCustomizer(NettyProperties nettyProperties) {
        this.nettyProperties = nettyProperties;
    }

    @Override
    public void customize(NettyServerSettings.NettyServerSettingsBuilder builder) {
        validateConfiguration();
        builder.serverId(nettyProperties.getId());
        builder.type(nettyProperties.getType());
        builder.port(nettyProperties.getPort());
        builder.threadNum(nettyProperties.getWorkerThreadNum());
        builder.businessThreadNum(nettyProperties.getBusinessThreadNum());
        builder.serializeType(nettyProperties.getSerializeType());
        applySLL(builder);
    }

    private void applySLL(NettyServerSettings.NettyServerSettingsBuilder builder) {
        NettyServerSettings.OpenSslSettings.OpenSslSettingsBuilder openSslSettingsBuilder = NettyServerSettings.OpenSslSettings.builder();
        NettyProperties.SslConfig sslConfig = nettyProperties.getSsl();
        if (sslConfig == null) {
            builder.sslSettings(openSslSettingsBuilder.build());
            return;
        }
        openSslSettingsBuilder.caPath(sslConfig.getCaPath());
        openSslSettingsBuilder.protocol(sslConfig.getProtocol());
        openSslSettingsBuilder.serverCrtPath(sslConfig.getServerCrtPath());
        openSslSettingsBuilder.serverPkcs8keyPath(sslConfig.getServerPkcs8keyPath());
        openSslSettingsBuilder.clientCrtPath(sslConfig.getClientCrtPath());
        openSslSettingsBuilder.clientPkcs8keyPath(sslConfig.getClientPkcs8keyPath());

        builder.sslSettings(openSslSettingsBuilder.build());
    }

    private void validateConfiguration() {
        if (nettyProperties.getId() == 0) {
            throw new IllegalStateException("netty.serverId not set value");
        }
        if (nettyProperties.getType() == 0) {
            throw new IllegalStateException("netty.type not set value");
        }
        if (nettyProperties.getPort() == 0) {
            throw new IllegalStateException("netty.port not set value");
        }
    }
}
