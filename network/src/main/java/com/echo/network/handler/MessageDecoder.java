package com.echo.network.handler;

import com.echo.network.message.InnerMessage;
import com.echo.network.message.OuterMessage;
import com.echo.network.message.ProtocolConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义协议消息解码
 *
 * @author li-yuanwen
 */
@Slf4j
public class MessageDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * 最大包长度,长度字段位移字节数,长度字段所占字节数
     **/
    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf buf = (ByteBuf) super.decode(ctx, in);
        if (buf == null) {
            return null;
        }

        // 释放in,否则会ByteBuf泄露,因为decode中
//        ReferenceCountUtil.release(in);

        short protocolHeaderIdentity = ProtocolConstant.getProtocolHeaderIdentity(buf);
        if (protocolHeaderIdentity == ProtocolConstant.PROTOCOL_INNER_HEADER_IDENTITY) {
            return InnerMessage.readIn(buf);
        }

        if (protocolHeaderIdentity == ProtocolConstant.PROTOCOL_OUTER_HEADER_IDENTITY) {
            return OuterMessage.readIn(buf);
        }

        log.warn("收到协议头[{}],暂不支持该协议", protocolHeaderIdentity);

        ctx.close();

        return null;
    }

    @Override
    protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
        // 重写,不增加ByteBuf引用
        return buffer.slice(index, length);
    }
}
