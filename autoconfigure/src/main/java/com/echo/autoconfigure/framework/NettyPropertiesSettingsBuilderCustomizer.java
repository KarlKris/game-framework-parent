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
        builder.port(nettyProperties.getPort());
        builder.threadNum(nettyProperties.getWorkerThreadNum());
        builder.businessThreadNum(nettyProperties.getBusinessThreadNum());
        builder.serializeType(nettyProperties.getSerializeType());
        applySLL(builder);
    }

    private void applySLL(NettyServerSettings.NettyServerSettingsBuilder builder) {
        NettyServerSettings.OpenSslSettings.OpenSslSettingsBuilder openSslSettingsBuilder = NettyServerSettings.OpenSslSettings.builder();
        openSslSettingsBuilder.caPath(nettyProperties.getSslCaPath());
        openSslSettingsBuilder.protocol(nettyProperties.getSslProtocol());
        openSslSettingsBuilder.serverCrtPath(nettyProperties.getSslServerCrtPath());
        openSslSettingsBuilder.serverPkcs8keyPath(nettyProperties.getSslServerPkcs8keyPath());
        openSslSettingsBuilder.clientCrtPath(nettyProperties.getSslClientCrtPath());
        openSslSettingsBuilder.clientPkcs8keyPath(nettyProperties.getSslClientPkcs8keyPath());

        builder.sslSettings(openSslSettingsBuilder.build());
    }
}
