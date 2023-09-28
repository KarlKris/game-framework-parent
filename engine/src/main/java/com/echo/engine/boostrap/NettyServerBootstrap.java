package com.echo.engine.boostrap;

import com.echo.common.util.IpUtils;
import com.echo.engine.config.NettyServerSettings;
import com.echo.engine.handler.NettyServerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * netty server boostrap
 *
 * @author: li-yuanwen
 */
@Slf4j
public class NettyServerBootstrap {


    /**
     * 配置信息
     **/
    private final NettyServerSettings settings;
    /**
     * ChannelInitializer
     **/
    private final NettyServerChannelInitializer initializer;


    /**
     * Reactor Acceptor线程
     **/
    private EventLoopGroup boss;
    /**
     * Reactor io线程池
     **/
    private EventLoopGroup workers;
    /**
     * handler线程池
     **/
    private EventExecutorGroup eventExecutorGroup;
    /**
     * 服务端channel
     **/
    private Channel channel;

    public NettyServerBootstrap(NettyServerSettings settings, NettyServerChannelInitializer initializer) {
        this.settings = settings;
        this.initializer = initializer;
    }


    public void startServer() throws InterruptedException {
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
        if (eventExecutorGroup != null) {
            this.eventExecutorGroup.shutdownGracefully();
        }
        log.warn("Netty 服务器[{}]正常关闭", settings.getPort());
    }


    private void initEventLoop() {
        // 使用Reactor主从多线程模型,一个Acceptor连接线程,I/O读写线程池,Handler线程池(NettyServerChannelInitializer)
        this.boss = createLoopGroup(1, "Netty-Acceptor-Thread");
        this.workers = createLoopGroup(settings.getIOThreadNum(), "Netty-IO-Thread");
        this.eventExecutorGroup = new DefaultEventExecutorGroup(settings.getHandleThreadNum()
                , new DefaultThreadFactory("Netty-Handler-Thread", true));
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

}
