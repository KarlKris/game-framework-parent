package com.echo.network.message;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ZipUtil;
import io.netty.buffer.ByteBuf;

/**
 * @author li-yuanwen
 * 自定义协议消息
 */
public class InnerMessage implements IMessage {

    /**
     * 服务器内部心跳消息包
     **/
    public static final InnerMessage HEART_BEAT_REQ = InnerMessage.of(
            InnerMessageHeader.of(ProtocolConstant.HEART_BEAT_REQ, null, false, 0, -1, null)
            , null);
    public static final InnerMessage HEART_BEAT_RES = InnerMessage.of(
            InnerMessageHeader.of(ProtocolConstant.HEART_BEAT_RES, null, false, 0, -1, null)
            , null);


    /**
     * 消息头
     **/
    private InnerMessageHeader header;
    /**
     * 消息体
     **/
    private byte[] body;

    @Override
    public short getProtocolHeaderIdentity() {
        return header.getProtocolHeader();
    }

    @Override
    public byte getMessageType() {
        return header.getType();
    }

    @Override
    public SocketProtocol getProtocol() {
        return header.getSocketProtocol();
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public long getSn() {
        return header.getSn();
    }

    public long getIdentity() {
        return header.getIdentity();
    }

    /**
     * 写入至ByteBuf
     **/
    public void writeTo(ByteBuf out) {
        header.writeTo(out);

        // 消息体有数据才写入
        if (ArrayUtil.isEmpty(body)) {
            return;
        }

        out.writeShort(body.length);
        out.writeBytes(body);
    }

    /**
     * 从ByteBuf中读取
     **/
    public static InnerMessage readIn(ByteBuf in) {
        InnerMessage message = new InnerMessage();
        message.header = InnerMessageHeader.readIn(in);
        if (in.readableBytes() > 0) {
            message.body = new byte[in.readShort()];
            in.readBytes(message.body);
            // 消息体解压缩
            if (message.isZip()) {
                message.body = ZipUtil.unGzip(message.body);
            }
        }
        return message;
    }

    public static InnerMessage of(InnerMessageHeader header, byte[] body) {
        InnerMessage message = new InnerMessage();
        message.header = header;
        message.body = body;
        return message;
    }

}
