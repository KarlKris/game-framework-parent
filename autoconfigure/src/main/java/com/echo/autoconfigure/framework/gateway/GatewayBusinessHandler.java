package com.echo.autoconfigure.framework.gateway;

import com.echo.engine.business.AbstractServerBusinessHandler;
import com.echo.engine.business.LocalMessageContext;
import com.echo.network.message.OuterMessage;
import com.echo.network.protocol.ChannelAttributeKeys;
import com.echo.network.protocol.ProtocolContext;
import com.echo.network.session.PlayerSession;
import com.echo.network.session.SessionContext;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;


/**
 * 网关服业务逻辑处理Handler
 *
 * @author li-yuanwen
 * @date 2021/12/8
 */
@Slf4j
@ChannelHandler.Sharable
public class GatewayBusinessHandler extends AbstractServerBusinessHandler<OuterMessage, PlayerSession> {

    public GatewayBusinessHandler(ProtocolContext protocolContext, SessionContext sessionContext
            , GatewayDispatcher dispatcher) {
        super(protocolContext, sessionContext, dispatcher);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, OuterMessage outerMessage) throws Exception {
        PlayerSession playerSession = (PlayerSession) ctx.channel().attr(ChannelAttributeKeys.SESSION).get();
        dispatcher.dispatch(playerSession, outerMessage);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        PlayerSession playerSession = sessionContext.registerPlayerSession(ctx.channel());

        if (log.isDebugEnabled()) {
            log.debug("与客户端[{}]建立连接,注册PlayerSession[{}]", playerSession.getIp(), playerSession.getSessionId());
        }

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        PlayerSession playerSession = sessionContext.removePlayerSession(ctx.channel());

        if (log.isDebugEnabled() && playerSession != null) {
            log.debug("与客户端[{}]断开连接,PlayerSession[{}]", playerSession.getIp(), playerSession.getSessionId());
        }

        if (playerSession != null) {
            if (playerSession.isIdentity()) {
                long id = playerSession.getIdentity();
                Executor executor = ctx.channel().eventLoop();
                executor.execute(() -> {
                    LocalMessageContext.setIdentity(id);
                    try {
                        //TODO 网关服需要考虑通知游戏服更新玩家断开链接
                    } finally {
                        LocalMessageContext.removeIdentity();
                    }
                });
            }
        }

        super.channelInactive(ctx);
    }
}
