package com.echo.autoconfigure.framework.game;

import com.echo.autoconfigure.framework.modules.AccountModule;
import com.echo.common.concurrency.IdentityRunnableLoopGroup;
import com.echo.engine.boostrap.NettyServerBootstrap;
import com.echo.engine.business.AbstractDispatcher;
import com.echo.engine.protocol.MessageFactory;
import com.echo.network.message.InnerMessage;
import com.echo.network.message.ProtocolConstant;
import com.echo.network.message.SocketProtocol;
import com.echo.network.session.ISession;
import com.echo.network.session.ServerSession;
import com.echo.network.session.SessionContext;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * 游戏服请求分发器
 *
 * @author: li-yuanwen
 */
public class GameDispatcher extends AbstractDispatcher<InnerMessage, ServerSession> {

    private final SessionContext sessionContext;
    private final MessageFactory messageFactory;
    private final IdentityRunnableLoopGroup executor;

    public GameDispatcher(NettyServerBootstrap bootstrap) {
        super(bootstrap.getProtocolContext());
        this.sessionContext = bootstrap.getSessionContext();
        this.messageFactory = bootstrap.getMessageFactory();
        this.executor = bootstrap.getBusinessLoopGroup();
    }

    @Override
    protected Executor findExecutor(ServerSession session, InnerMessage message) {
        long identity = findIdentity(session, message);
        if (identity > 0) {
            return executor.getExecutor(identity);
        }
        return executor.next();
    }

    @Override
    protected long findIdentity(ServerSession session, InnerMessage message) {
        return message.getIdentity();
    }

    @Override
    protected void response(ServerSession session, InnerMessage message, SocketProtocol protocol, byte[] responseBody) {
        InnerMessage innerMessage = messageFactory.toInnerMessage(message.getSn()
                , ProtocolConstant.transformResponse(message.getMessageType())
                , protocol
                , responseBody
                , -1L
                , null);

        session.writeAndFlush(innerMessage);
    }

    @Override
    protected boolean beforeHandle(ServerSession session, InnerMessage message) {
        // 放行登陆协议,登陆逻辑需实现挤人功能
        SocketProtocol protocol = message.getProtocol();
        if (protocol.getModule() == AccountModule.MODULE
                && protocol.getMethodId() == AccountModule.LOGON_ACCOUNT) {
            return true;
        }

        ISession identitySession = sessionContext.getIdentitySession(message.getIdentity());
        // 重新绑定,处理网关服与游戏服重连的情况下,玩家Session信息丢失后重新绑定
        if (identitySession == null) {
            sessionContext.bindIdentity(session, message.getIdentity());
            return true;
        }

        return Objects.equals(session.getSessionId(), identitySession.getSessionId());
    }
}
