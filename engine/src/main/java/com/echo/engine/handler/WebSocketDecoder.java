package com.echo.engine.handler;

import com.echo.network.message.InnerMessage;
import com.echo.network.message.OuterMessage;
import com.echo.network.message.ProtocolConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * WebSocket 消息解码器
 *
 * @author li-yuanwen
 * @date 2021/7/29 23:06
 **/
@Slf4j
public class WebSocketDecoder extends MessageToMessageDecoder<WebSocketFrame> {

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception {
        ByteBuf buf = msg.content();
        short protocolHeaderIdentity = ProtocolConstant.getProtocolHeaderIdentity(buf);
        if (protocolHeaderIdentity == ProtocolConstant.PROTOCOL_INNER_HEADER_IDENTITY) {
            InnerMessage innerMessage = InnerMessage.readIn(buf);
            ctx.fireChannelRead(innerMessage);
        } else if (protocolHeaderIdentity == ProtocolConstant.PROTOCOL_OUTER_HEADER_IDENTITY) {
            OuterMessage outerMessage = OuterMessage.readIn(buf);
            ctx.fireChannelRead(outerMessage);
        } else {
            log.warn("收到协议头[{}],暂不支持该协议", protocolHeaderIdentity);
            ctx.close();
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            if (log.isDebugEnabled()) {
                log.info("websocket 握手成功。");
            }
            WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            if (log.isDebugEnabled()) {
                handshakeComplete.requestHeaders().forEach(entry -> log.info("header key:[{}] value:[{}]", entry.getKey(), entry.getValue()));
            }

        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
