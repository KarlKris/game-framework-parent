package com.echo.engine.handler;

import com.echo.engine.boostrap.NettyServerBootstrap;
import com.echo.engine.config.NettyServerSettings;
import com.echo.network.handler.HeartBeatHandler;
import com.echo.network.handler.MessageDecoder;
import com.echo.network.handler.MessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author li-yuanwen
 */
@Slf4j
public class NettyClientChannelInitializer extends ChannelInitializer<SocketChannel> {


    private final NettyServerSettings settings;
    private final MessageEncoder messageEncoder;
    private final ClientBusinessHandler businessHandler;

    private final HeartBeatHandler heartBeatHandler;

    private final SslContext sslContext;


    public NettyClientChannelInitializer(NettyServerSettings settings
            , MessageEncoder messageEncoder
            , ClientBusinessHandler businessHandler
            , HeartBeatHandler heartBeatHandler) throws SSLException {
        this.settings = settings;
        this.messageEncoder = messageEncoder;
        this.businessHandler = businessHandler;
        this.heartBeatHandler = heartBeatHandler;
        this.sslContext = tryInitSslContext();
    }

    private SslContext tryInitSslContext() throws SSLException {
        if (settings.getSslSettings().getProtocol() == null) {
            return null;
        }
        return newClientSslContext(settings.getSslSettings());
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        if (this.sslContext != null) {
            SSLEngine sslEngine = sslContext.newEngine(ch.alloc());
            sslEngine.setUseClientMode(true);
            pipeline.addFirst(SslHandler.class.getSimpleName()
                    , new SslHandler(sslEngine));
        }

        // 编解码器
        pipeline.addLast(MessageEncoder.class.getSimpleName(), this.messageEncoder);
        pipeline.addLast(MessageDecoder.class.getSimpleName(), NettyServerBootstrap.messageDecoder());

        // 心跳
        pipeline.addLast(IdleStateHandler.class.getSimpleName(), newIdleStateHandler());
        pipeline.addLast(HeartBeatHandler.class.getSimpleName(), this.heartBeatHandler);

        // 业务
        pipeline.addLast(ClientBusinessHandler.class.getSimpleName(), this.businessHandler);

    }


    private IdleStateHandler newIdleStateHandler() {
        return new IdleStateHandler(0, settings.getClientWriterIdleTime(), 0, TimeUnit.SECONDS);
    }

    private SslContext newClientSslContext(NettyServerSettings.OpenSslSettings settings) throws SSLException {
        // 服务端所需证书
        File certChainFile = new File(settings.getClientCrtPath());
        File keyFile = new File(settings.getClientPkcs8keyPath());
        File rootFile = new File(settings.getCaPath());
        return SslContextBuilder.forClient()
                .keyManager(certChainFile, keyFile)
                .trustManager(rootFile)
                .build();
    }
}
