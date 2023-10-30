package com.echo.engine.boostrap;

import com.echo.common.concurrency.IdentityRunnableLoopGroup;
import com.echo.common.util.IpUtils;
import com.echo.engine.config.NettyServerSettings;
import com.echo.engine.handler.NettyServerChannelInitializer;
import com.echo.engine.protocol.MessageFactory;
import com.echo.engine.rpc.core.GenericRemoteLocalServerSeekOperation;
import com.echo.engine.rpc.core.InvocationContext;
import com.echo.engine.rpc.core.RemoteServerSeekOperation;
import com.echo.engine.rpc.push.PushOperation;
import com.echo.network.handler.HeartBeatHandler;
import com.echo.network.handler.MessageDecoder;
import com.echo.network.handler.MessageEncoder;
import com.echo.network.protocol.ProtocolContext;
import com.echo.network.serialize.SerializeType;
import com.echo.network.session.SessionContext;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;

/**
 * netty server boostrap
 *
 * @author: li-yuanwen
 */
@Slf4j
public class NettyServerBootstrap {

    /**
     * 消息序列方式
     **/
    public static SerializeType SERIALIZE_TYPE = SerializeType.PROTOBUF;

    /**
     * 配置信息
     **/
    private final NettyServerSettings settings;
    /**
     * ChannelInitializer
     **/
    private NettyServerChannelInitializer initializer;

    /**
     * 心跳检测
     **/
    private final HeartBeatHandler heartBeatHandler = new HeartBeatHandler(true);

    /**
     * 消息工厂
     **/
    private final MessageFactory messageFactory;

    private final MessageEncoder messageEncoder = new MessageEncoder();

    // --------------------------------------------------------------

    /**
     * 协议容器
     **/
    private final ProtocolContext protocolContext = new ProtocolContext();
    /**
     * rpc调用消息容器
     **/
    private final InvocationContext invocationContext = new InvocationContext();
    /**
     * session容器
     */
    private final SessionContext sessionContext = new SessionContext();
    /**
     * 推送服务
     */
    private final PushOperation pushOperation;
    /**
     * 服务发现
     **/
    private final RemoteServerSeekOperation remoteServerSeekOperation;

    // --------------------------------------------------------------

    /**
     * Reactor Acceptor线程
     **/
    private EventLoopGroup boss;
    /**
     * Reactor io线程池
     **/
    private EventLoopGroup workers;
    /**
     * 业务线程池
     **/
    private IdentityRunnableLoopGroup businessLoopGroup;
    /**
     * 客户端线程池
     **/
    private EventLoopGroup clientEventLoopGroup;
    /**
     * 服务端channel
     **/
    private Channel channel;

    public NettyServerBootstrap(NettyServerSettings settings) throws SSLException {
        this.settings = settings;
        this.messageFactory = new MessageFactory(settings);
        this.pushOperation = new PushOperation(sessionContext, messageFactory);
        this.remoteServerSeekOperation = new GenericRemoteLocalServerSeekOperation(this);
        SerializeType serializeType = SerializeType.valueOf(settings.getSerializeType());
        if (serializeType == null) {
            throw new RuntimeException("invalid serialize type: " + settings.getSerializeType());
        }
        SERIALIZE_TYPE = serializeType;
    }


    public void setInitializer(NettyServerChannelInitializer initializer) {
        this.initializer = initializer;
    }

    public void startServer() throws InterruptedException {
        if (initializer == null) {
            throw new RuntimeException("not set NettyServerChannelInitializer");
        }

        initEventLoop();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(this.boss, this.workers)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                // 发送队列流控
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT)
                // ChannelOption.SO_BACKLOG对应的是tcp/ip协议listen函数中的backlog参数
                // 函数listen(int socketfd,int backlog)用来初始化服务端可连接队列
                // 服务端处理客户端连接请求是顺序处理的，所以同一时间只能处理一个客户端连接
                // 多个客户端来的时候，服务端将不能处理的客户端连接请求放在队列中等待处理，backlog参数指定了队列的大小
                .option(ChannelOption.SO_BACKLOG, settings.getBackLog())
                // 这个参数表示允许重复使用本地地址和端口
                // 某个服务器进程占用了TCP的80端口进行监听，此时再次监听该端口就会返回错误
                // 使用该参数就可以解决问题，该参数允许共用该端口，这个在服务器程序中比较常使用
                .option(ChannelOption.SO_REUSEADDR, true)
                // 重用缓存区
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(initializer);

        // 同步绑定端口
        ChannelFuture channelFuture = serverBootstrap.bind(settings.getPort()).sync();
        // 绑定服务器Channel
        this.channel = channelFuture.channel();

        if (log.isInfoEnabled()) {
            log.info("Netty 服务器[{}]正常启动成功", settings.getPort());
        }
    }

    public void shutdown() {
        this.channel.close();
        this.boss.shutdownGracefully();
        this.workers.shutdownGracefully();
        this.clientEventLoopGroup.shutdownGracefully();
        this.businessLoopGroup.shutdownGracefully();
        log.warn("Netty 服务器[{}]正常关闭", settings.getPort());
    }


    private void initEventLoop() {
        // 使用Reactor主从多线程模型,一个Acceptor连接线程,I/O读写线程池,Handler线程池(NettyServerChannelInitializer)
        this.boss = createLoopGroup(1, "Netty-Acceptor-Thread");
        this.workers = createLoopGroup(settings.getIOThreadNum(), "Netty-IO-Thread");
        this.businessLoopGroup = new IdentityRunnableLoopGroup(settings.getFinalBusinessThreadNum());
        this.clientEventLoopGroup = createLoopGroup(Runtime.getRuntime().availableProcessors(), "Netty-Client-Thread");
    }

    private boolean useEpoll() {
        return IpUtils.isLinuxPlatform() && Epoll.isAvailable();
    }

    private EventLoopGroup createLoopGroup(int threadNum, String threadPrefix) {
        if (useEpoll()) {
            return new EpollEventLoopGroup(threadNum, new DefaultThreadFactory(threadPrefix, true));
        }
        return new NioEventLoopGroup(threadNum, new DefaultThreadFactory(threadPrefix, true));
    }

    public NettyServerSettings getSettings() {
        return settings;
    }

    public HeartBeatHandler getHeartBeatHandler() {
        return heartBeatHandler;
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    public MessageEncoder getMessageEncoder() {
        return messageEncoder;
    }

    public ProtocolContext getProtocolContext() {
        return protocolContext;
    }

    public InvocationContext getInvocationContext() {
        return invocationContext;
    }

    public SessionContext getSessionContext() {
        return sessionContext;
    }

    public PushOperation getPushOperation() {
        return pushOperation;
    }

    public RemoteServerSeekOperation getRemoteServerSeekOperation() {
        return remoteServerSeekOperation;
    }

    public EventLoopGroup getClientEventLoopGroup() {
        return clientEventLoopGroup;
    }

    public IdentityRunnableLoopGroup getBusinessLoopGroup() {
        return businessLoopGroup;
    }

    /**
     * 消息最大长度 10M
     **/
    final static int DEFAULT_MAX_MESSAGE_CONTENT_LENGTH = 1024 * 1024 * 10;

    public static MessageDecoder messageDecoder() {
        return messageDecoder(DEFAULT_MAX_MESSAGE_CONTENT_LENGTH);
    }

    public static MessageDecoder messageDecoder(int maxMessageLength) {
        return new MessageDecoder(maxMessageLength, 2, 4);
    }

}
