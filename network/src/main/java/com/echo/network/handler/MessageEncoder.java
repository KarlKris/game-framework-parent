package com.echo.network.handler;

import com.echo.network.message.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义协议编码器
 *
 * @author li-yuanwen
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageEncoder extends MessageToByteEncoder<IMessage> {

    @Override
    public void encode(ChannelHandlerContext ctx, IMessage msg, ByteBuf out) throws Exception {
        short protocolHeaderIdentity = msg.getProtocolHeaderIdentity();
        if (protocolHeaderIdentity == ProtocolConstant.PROTOCOL_OUTER_HEADER_IDENTITY) {
            ((OuterMessage) msg).writeTo(out);
            // ByteBuf 长度字段是排在协议头字段之后,即index为2,长度字节为int 即除去长度字段+协议头字段 剩余的就是长度
            int length = out.readableBytes() - 6;
            out.setInt(2, length);

            if (log.isDebugEnabled()) {
                SocketProtocol protocol = msg.getProtocol();
                log.debug("向连接[{}]发送外部消息,消息类型[{}],消息长度[{}],消息命令[{}-{}]"
                        , ctx.channel().remoteAddress()
                        , msg.getMessageType()
                        , length
                        , protocol == null ? 0 : protocol.getModule()
                        , protocol == null ? 0 : protocol.getMethodId());
            }

            return;
        }

        if (protocolHeaderIdentity == ProtocolConstant.PROTOCOL_INNER_HEADER_IDENTITY) {
            ((InnerMessage) msg).writeTo(out);
            // ByteBuf 长度字段是排在协议头字段之后,即index为2,长度字节为int 即除去长度字段+协议头字段 剩余的就是长度
            int length = out.readableBytes() - 6;
            out.setInt(2, length);

            if (log.isDebugEnabled()) {
                SocketProtocol protocol = msg.getProtocol();
                log.debug("向连接[{}]发送内部消息,消息类型[{}],消息长度[{}],消息命令[{}-{}]"
                        , ctx.channel().remoteAddress()
                        , msg.getMessageType()
                        , length
                        , protocol == null ? 0 : protocol.getModule()
                        , protocol == null ? 0 : protocol.getMethodId());
            }
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("编码非传输消息类型[{}],协议头[{}],忽略", msg.getClass().getSimpleName(), protocolHeaderIdentity);
        }
    }
}
