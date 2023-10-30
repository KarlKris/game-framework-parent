package com.echo.engine.handler;

import com.echo.engine.boostrap.NettyServerBootstrap;
import com.echo.engine.business.AbstractServerBusinessHandler;
import com.echo.engine.config.NettyServerSettings;
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
 * netty 服务端childhandler
 *
 * @author: li-yuanwen
 */
@Slf4j
public class NettyServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * NettyServerBootstrap
     **/
    private final NettyServerBootstrap bootstrap;
    /**
     * 协议处理器
     **/
    private final AbstractServerBusinessHandler<?, ?> businessHandler;

    /**
     * sslContext
     **/
    private final SslContext sslContext;

    public NettyServerChannelInitializer(NettyServerBootstrap bootstrap, AbstractServerBusinessHandler<?, ?> businessHandler) throws SSLException {
        this.bootstrap = bootstrap;
        this.businessHandler = businessHandler;
        this.sslContext = tryInitSslContext();
    }


    private SslContext tryInitSslContext() throws SSLException {
        if (bootstrap.getSettings().getSslSettings().getProtocol() == null) {
            return null;
        }
        return newServerSslContext(bootstrap.getSettings().getSslSettings());
    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // SSL 认证
        if (sslContext != null) {
            SSLEngine sslEngine = sslContext.newEngine(ch.alloc());
            pipeline.addFirst(SslHandler.class.getSimpleName(), new SslHandler(sslEngine));
        }

        //TODO 过滤器,防火墙

        // 协议选择
        pipeline.addLast(ProtocolSelectorHandler.class.getSimpleName()
                , new ProtocolSelectorHandler(bootstrap.getHeartBeatHandler(), bootstrap.getMessageEncoder()));

        // 服务端心跳检测
        pipeline.addLast(IdleStateHandler.class.getSimpleName(), newIdleStateHandler());

        // 业务处理
        pipeline.addLast(AbstractServerBusinessHandler.HANDLER_NAME, businessHandler);
    }


    private SslContext newServerSslContext(NettyServerSettings.OpenSslSettings settings) throws SSLException {
        // 服务端所需证书
        File certChainFile = new File(settings.getServerCrtPath());
        File keyFile = new File(settings.getServerPkcs8keyPath());
        File rootFile = new File(settings.getCaPath());

        return SslContextBuilder.forServer(certChainFile, keyFile)
                .trustManager(rootFile)
                .build();
    }

    private IdleStateHandler newIdleStateHandler() {
        return new IdleStateHandler(bootstrap.getSettings().getServerReaderIdleTime(), 0, 0, TimeUnit.SECONDS);
    }
}
