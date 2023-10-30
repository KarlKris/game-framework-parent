package com.echo.network.message;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ZipUtil;
import io.netty.buffer.ByteBuf;

/**
 * @author li-yuanwen
 * 外部自定义协议消息
 */
public class OuterMessage implements IMessage {

    /**
     * 服务器外部心跳消息包
     **/
    public static final OuterMessage HEART_BEAT_REQ = OuterMessage.of(
            OuterMessageHeader.of(0, ProtocolConstant.HEART_BEAT_REQ, null, false)
            , null);
    public static final OuterMessage HEART_BEAT_RES = OuterMessage.of(
            OuterMessageHeader.of(0, ProtocolConstant.HEART_BEAT_RES, null, false)
            , null);

    /**
     * 协议头
     **/
    private OuterMessageHeader header;
    /**
     * 协议体
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

    /**
     * 写入至ByteBuf
     **/
    public void writeTo(ByteBuf out) {
        header.writeTo(out);

        // 消息体有数据才写入
        if (ArrayUtil.isEmpty(body)) {
            return;
        }

        // body字节长度32767
        out.writeShort(body.length);
        out.writeBytes(body);
    }

    /**
     * 从ByteBuf中读取
     **/
    public static OuterMessage readIn(ByteBuf in) {
        OuterMessage message = new OuterMessage();
        message.header = OuterMessageHeader.readIn(in);

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

    public static OuterMessage of(OuterMessageHeader header, byte[] body) {
        OuterMessage message = new OuterMessage();
        message.header = header;
        message.body = body;
        return message;
    }
}
