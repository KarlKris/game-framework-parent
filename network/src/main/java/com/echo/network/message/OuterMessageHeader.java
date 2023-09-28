package com.echo.network.message;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

/**
 * @author li-yuanwen
 * 用于外部自定义协议消息头
 */
@Getter
public class OuterMessageHeader {

    /**
     * 协议头
     **/
    private short protocolHeader = ProtocolConstant.PROTOCOL_OUTER_HEADER_IDENTITY;
    /**
     * 消息字节长度
     **/
    private int length;
    /**
     * 消息序号
     **/
    private long sn;
    /**
     * 消息类型
     **/
    private byte type;
    /**
     * 请求业务协议号
     **/
    private SocketProtocol socketProtocol;
    /**
     * 消息体压缩标识(true为压缩)
     **/
    private boolean zip;
    /**
     * 消息体序列化标识
     **/
    private byte serializeType;

    /**
     * 写入至ByteBuf
     **/
    void writeTo(ByteBuf out) {
        out.writeShort(protocolHeader);
        // 长度占位
        out.writeInt(0);
        out.writeLong(sn);

        // 添加序列化类型
        type = ProtocolConstant.addSerializeType(type, serializeType);

        // 加入压缩标识
        if (zip) {
            type = ProtocolConstant.addBodyZipState(type);
        }

        out.writeByte(type);

        // 协议标识
        if (ProtocolConstant.hasState(type, ProtocolConstant.COMMAND_MARK)) {
            socketProtocol.write(out);
        }
    }

    /**
     * 从ByteBuf中读取
     **/
    public static OuterMessageHeader readIn(ByteBuf in) {
        OuterMessageHeader header = new OuterMessageHeader();
        header.protocolHeader = in.readShort();
        header.length = in.readInt();
        header.sn = in.readLong();
        header.type = in.readByte();

        if (ProtocolConstant.hasState(header.type, ProtocolConstant.COMMAND_MARK)) {
            header.socketProtocol = SocketProtocol.read(in);
        }

        header.zip = ProtocolConstant.hasState(header.type, ProtocolConstant.BODY_ZIP_MARK);
        header.serializeType = ProtocolConstant.getSerializeType(header.type);

        return header;
    }


    public static OuterMessageHeader of(long sn, byte type, SocketProtocol socketProtocol, boolean zip, byte serializeType) {
        OuterMessageHeader header = new OuterMessageHeader();
        header.sn = sn;
        header.type = type;
        header.socketProtocol = socketProtocol;
        header.zip = zip;
        header.serializeType = serializeType;
        return header;
    }

}
