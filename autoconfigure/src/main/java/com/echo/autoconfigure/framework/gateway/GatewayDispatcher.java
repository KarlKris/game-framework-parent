package com.echo.autoconfigure.framework.gateway;

import com.echo.common.concurrency.IdentityRunnableLoopGroup;
import com.echo.engine.boostrap.NettyServerBootstrap;
import com.echo.engine.business.AbstractDispatcher;
import com.echo.engine.client.NettyClient;
import com.echo.engine.protocol.MessageFactory;
import com.echo.engine.rpc.core.ForwardInvocation;
import com.echo.engine.rpc.core.InvocationContext;
import com.echo.engine.rpc.core.RemoteServerSeekOperation;
import com.echo.network.message.InnerMessage;
import com.echo.network.message.OuterMessage;
import com.echo.network.message.ProtocolConstant;
import com.echo.network.message.SocketProtocol;
import com.echo.network.protocol.ProtocolMethod;
import com.echo.network.session.PlayerSession;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

/**
 * 网关服分发
 *
 * @author: li-yuanwen
 */
@Slf4j
public class GatewayDispatcher extends AbstractDispatcher<OuterMessage, PlayerSession> {


    private final RemoteServerSeekOperation remoteServerSeekOperation;

    private final IdentityRunnableLoopGroup executor;
    private final MessageFactory messageFactory;

    private final InvocationContext invocationContext;


    public GatewayDispatcher(NettyServerBootstrap bootstrap) {
        super(bootstrap.getProtocolContext());
        this.remoteServerSeekOperation = bootstrap.getRemoteServerSeekOperation();
        this.executor = bootstrap.getBusinessLoopGroup();
        this.messageFactory = bootstrap.getMessageFactory();
        this.invocationContext = bootstrap.getInvocationContext();
    }

    @Override
    protected Executor findExecutor(PlayerSession session, OuterMessage message) {
        if (session.isIdentity()) {
            return executor.getExecutor(session.getIdentity());
        }
        return executor.next();
    }

    @Override
    protected long findIdentity(PlayerSession session, OuterMessage message) {
        return session.getIdentity();
    }

    @Override
    protected void response(PlayerSession session, OuterMessage message, SocketProtocol protocol, byte[] responseBody) {
        OuterMessage outerMessage = messageFactory.toOuterMessage(message.getSn()
                , ProtocolConstant.transformResponse(message.getMessageType())
                , protocol
                , responseBody);

        session.writeAndFlush(outerMessage);
    }

    @Override
    protected boolean forwardMessage(PlayerSession session, OuterMessage message) {
        if (!session.isIdentity()) {
            if (log.isDebugEnabled()) {
                log.debug("连接Session[{}]未绑定身份标识,忽略本次转发", session.getIp());
            }
            return false;
        }

        ProtocolMethod protocolMethod = protocolContext.getProtocol(message.getProtocol());
        if (protocolMethod.getType() == 0) {
            if (log.isDebugEnabled()) {
                log.debug("连接Session[{}]消息协议[{}]未指定业务所在服务类型,忽略本次转发", session.getIp(), message.getProtocol());
            }
            return false;
        }

        NettyClient nettyClient = remoteServerSeekOperation.seekApplication(protocolMethod.getType(), session.getIdentity());
        long nextSn = invocationContext.nextSn();
        InnerMessage innerMessage = messageFactory.convertToRequestInnerMessage(session, nextSn, message);
        final long identity = session.getIdentity();

        ForwardInvocation forwardInvocation = new ForwardInvocation(innerMessage.getSn()
                , message.getSn(), identity, session, messageFactory);

        try {
            nettyClient.send(innerMessage, forwardInvocation);
            return true;
        } catch (InterruptedException e) {
            log.error("消息转发至[{}]发生未知异常", nettyClient.getAddress(), e);
            return false;
        }
    }
}
