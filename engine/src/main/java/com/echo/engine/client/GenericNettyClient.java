package com.echo.engine.client;

import cn.hutool.core.annotation.AnnotationUtil;
import com.echo.engine.handler.NettyClientChannelInitializer;
import com.echo.engine.protocol.MessageFactory;
import com.echo.engine.rpc.core.Invocation;
import com.echo.engine.rpc.core.InvocationContext;
import com.echo.network.anno.SocketController;
import com.echo.network.message.IMessage;
import com.echo.network.protocol.ProtocolContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Netty Client 通用实现
 *
 * @author: li-yuanwen
 */
@Slf4j
public class GenericNettyClient implements NettyClient {


    /**
     * 连接目标IP地址
     **/
    private final Address address;
    /**
     * 连接超时(毫秒)
     **/
    private int connectTimeoutMillis = 3000;
    /**
     * 共享线程组
     **/
    private final EventLoopGroup eventLoopGroup;

    private final NettyClientChannelInitializer channelInitializer;

    /**
     * rpc调用容器
     **/
    private final InvocationContext invocationContext;
    /**
     * 消息工厂
     **/
    private final MessageFactory messageFactory;
    /**
     * 协议容器
     **/
    private final ProtocolContext protocolContext;


    /**
     * Channel
     **/
    private Channel channel;
    /**
     * 代理对象
     **/
    private final Map<String, Object> proxy = new HashMap<>();

    public GenericNettyClient(Address address
            , EventLoopGroup eventLoopGroup
            , NettyClientChannelInitializer channelInitializer
            , InvocationContext invocationContext
            , ProtocolContext protocolContext
            , MessageFactory messageFactory) {
        this.address = address;
        this.eventLoopGroup = eventLoopGroup;
        this.channelInitializer = channelInitializer;
        this.invocationContext = invocationContext;
        this.messageFactory = messageFactory;
        this.protocolContext = protocolContext;
    }

    private void connect() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(this.eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.connectTimeoutMillis)
                .handler(this.channelInitializer);

        ChannelFuture channelFuture = bootstrap.connect(this.address.getIp(), this.address.getPort());
        this.channel = channelFuture.channel();
        channelFuture.sync();

        log.warn("客户端连接[{}:{}]成功", this.address.getIp(), this.address.getPort());

    }

    private boolean isConnected() {
        return this.channel != null && this.channel.isActive();
    }

    @Override
    public void send(IMessage message, Invocation invocation) throws InterruptedException {
        if (!isConnected()) {
            connect();
        }

        channel.writeAndFlush(message);
        invocationContext.addInvocation(invocation);
    }

    @Override
    public <T> T getSendProxy(Class<T> clz) {
        String name = clz.getName();
        Object target = this.proxy.get(name);
        if (target != null) {
            return (T) target;
        }

        synchronized (this.proxy) {
            target = this.proxy.get(name);
            if (target != null) {
                return (T) target;
            }

            SocketController socketController = AnnotationUtil.getAnnotation(clz, SocketController.class);
            if (socketController == null) {
                throw new RuntimeException(clz.getSimpleName() + "不是远程协议接口");
            }

            target = Proxy.newProxyInstance(clz.getClassLoader()
                    , new Class[]{clz}
                    , new SendProxyInvoker(this, invocationContext, protocolContext, messageFactory, 5));

            this.proxy.put(name, target);
        }

        return (T) target;
    }

    @Override
    public Address getAddress() {
        return address;
    }
}
