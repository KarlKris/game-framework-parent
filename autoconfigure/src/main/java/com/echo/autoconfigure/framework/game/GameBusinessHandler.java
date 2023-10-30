package com.echo.autoconfigure.framework.game;

import com.echo.engine.business.AbstractServerBusinessHandler;
import com.echo.network.message.InnerMessage;
import com.echo.network.protocol.ChannelAttributeKeys;
import com.echo.network.protocol.ProtocolContext;
import com.echo.network.session.ServerSession;
import com.echo.network.session.SessionContext;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 游戏服业务逻辑处理Handler
 *
 * @author: li-yuanwen
 */
@Slf4j
@ChannelHandler.Sharable
public class GameBusinessHandler extends AbstractServerBusinessHandler<InnerMessage, ServerSession> {


    public GameBusinessHandler(ProtocolContext protocolContext
            , SessionContext sessionContext, GameDispatcher dispatcher) {
        super(protocolContext, sessionContext, dispatcher);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InnerMessage innerMessage) throws Exception {
        ServerSession serverSession = (ServerSession) ctx.channel().attr(ChannelAttributeKeys.SESSION).get();
        dispatcher.dispatch(serverSession, innerMessage);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ServerSession serverSession = sessionContext.registerServerSession(ctx.channel());
        if (log.isDebugEnabled()) {
            log.debug("与客户端[{}]建立连接,注册PlayerSession[{}]", serverSession.getIp(), serverSession.getSessionId());
        }

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ServerSession serverSession = sessionContext.removeServerSession(ctx.channel());

        if (serverSession != null) {
            if (log.isDebugEnabled()) {
                log.debug("与客户端[{}]断开连接,PlayerSession[{}]", serverSession.getIp(), serverSession.getSessionId());
            }

            for (long id : serverSession.getIdentities()) {
                sessionContext.logout(id);
            }
        }

        super.channelInactive(ctx);
    }
}
