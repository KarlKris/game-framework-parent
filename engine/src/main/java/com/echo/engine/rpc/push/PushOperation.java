package com.echo.engine.rpc.push;

import com.echo.engine.boostrap.NettyServerBootstrap;
import com.echo.engine.protocol.MessageFactory;
import com.echo.network.message.*;
import com.echo.network.session.ISession;
import com.echo.network.session.SessionContext;
import com.echo.network.utils.SerializeUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 推送服务
 *
 * @author: li-yuanwen
 */
@Slf4j
public class PushOperation {

    private final SessionContext sessionContext;

    private final MessageFactory messageFactory;

    public PushOperation(SessionContext sessionContext, MessageFactory messageFactory) {
        this.sessionContext = sessionContext;
        this.messageFactory = messageFactory;
    }

    public SessionContext getSessionContext() {
        return sessionContext;
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    public void pushOuterMessage(PushResponse pushResponse, SocketProtocol protocol) {
        byte[] content = pushResponse.getContent();

        for (long identity : pushResponse.getTargets()) {
            ISession session = sessionContext.getIdentitySession(identity);
            if (session == null) {
                continue;
            }

            OuterMessage outerMessage = messageFactory.toOuterMessage(0L
                    , ProtocolConstant.BUSINESS_REQ
                    , protocol
                    , content);

            if (log.isDebugEnabled()) {
                log.debug("推送消息至外网[{},{}]", outerMessage.getSn(), outerMessage.getProtocol());
            }

            session.writeAndFlush(outerMessage);
        }
    }

    public void pushInnerMessage(ISession session, PushResponse pushResponse, SocketProtocol protocol) {
        byte[] body = SerializeUtils.serialize(NettyServerBootstrap.SERIALIZE_TYPE, pushResponse);
        InnerMessage message = messageFactory.toInnerMessage(0L
                , ProtocolConstant.BUSINESS_REQ
                , protocol
                , body
                , -1L
                , session.getIp());

        if (log.isDebugEnabled()) {
            log.debug("推送消息至内网[{},{}-{}]", message.getSn()
                    , message.getProtocol().getModule()
                    , message.getProtocol().getMethodId());
        }

        session.writeAndFlush(message);
    }


}
