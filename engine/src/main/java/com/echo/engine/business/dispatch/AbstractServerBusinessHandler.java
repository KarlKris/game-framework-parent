package com.echo.engine.business.dispatch;

import com.echo.network.message.IMessage;
import com.echo.network.protocol.ProtocolContext;
import com.echo.network.session.ISession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 抽象服务端的业务逻辑处理ChannelHandler基类
 *
 * @author: li-yuanwen
 */
@Slf4j
public abstract class AbstractServerBusinessHandler<M extends IMessage, S extends ISession> extends SimpleChannelInboundHandler<M> {

    public static final String HANDLER_NAME = "SERVER_BUSINESS_HANDLER";

    /**
     * 协议容器
     **/
    protected final ProtocolContext protocolContext;
    /**
     * 协议分发处理器
     **/
    protected final Dispatcher<M, S> dispatcher;

    public AbstractServerBusinessHandler(ProtocolContext protocolContext, Dispatcher<M, S> dispatcher) {
        this.protocolContext = protocolContext;
        this.dispatcher = dispatcher;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            log.error("服务器发生IOException,与客户端[{}]断开连接", ctx.channel().id(), cause);
        } else {
            log.error("服务器发生未知异常", cause);
        }
        ctx.close();
    }
}
