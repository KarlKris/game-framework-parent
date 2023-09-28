package com.echo.network.message;

import io.netty.buffer.ByteBuf;

/**
 * 协议常量
 *
 * @author li-yuanwen
 */
public interface ProtocolConstant {

    /**
     * 内部协议头标识
     **/
    short PROTOCOL_INNER_HEADER_IDENTITY = 0x24;
    /**
     * 外部协议头标识
     **/
    short PROTOCOL_OUTER_HEADER_IDENTITY = 0x08;


    /**
     * 读取协议头标识
     *
     * @param in ByteBuf
     * @return /
     */
    static short getProtocolHeaderIdentity(ByteBuf in) {
        // 标记读位置
        in.markReaderIndex();
        short protocolHeaderIdentity = in.readShort();
        in.resetReaderIndex();
        return protocolHeaderIdentity;
    }

    // ------------- 消息类型 -------------------------------

    /**
     * 消息类型对应于消息头#type(范围-128-127) 取1->127 即最高位符号均取0
     * 8个字节=0 + 1位(消息体是否压缩 0未压缩 1已压缩) + 2位序列化方式 + 1位是否携带命令(0不携带命令,1携带命令) + 3位消息类型(1位类型(0 请求 1 响应) + 2位具体类型)
     * 其中消息类型必须确认是否会携带命令,即携带命令和不携带命令的消息类型各可有7个,若后续不够,可扩展为short类型
     */

    /**
     * 携带命令 0 0 00 1 000
     **/
    byte COMMAND_MARK = 0x8;

    /**
     * 消息压缩 0 1 00 0 000
     **/
    byte BODY_ZIP_MARK = 0x40;

    /**
     * 具体消息类型掩码 0 0 00 1 111
     **/
    byte MESSAGE_TYPE_MARK = 0xf;

    /**
     * 序列化方式掩码 0 0 11 0 000
     **/
    byte SERIALIZE_TYPE_MARK = 0x30;

    /**
     * 请求/响应掩码 0 0 00 0 1 00
     **/
    byte REQ_RES_TYPE_MARK = 0x4;

    // 具体消息类型

    // 不携带命令消息类型

    /**
     * 心跳检测请求(不携带命令) 0 0 00 0 0 00
     **/
    byte HEART_BEAT_REQ = 0x0;

    /**
     * 心跳检测响应(不携带命令) 0 0 00 0 1 00
     **/
    byte HEART_BEAT_RES = 0x4;

    // 携带命令消息类型

    /**
     * 业务请求(携带命令) 0 0 00 1 0 00
     **/
    byte BUSINESS_REQ = 0x8;

    /**
     * 业务响应(携带命令) 0 0 00 1 1 00
     **/
    byte BUSINESS_RES = 0xc;


    /**
     * 加上消息压缩标识
     *
     * @param type 消息类型
     * @return /
     */
    static byte addBodyZipState(byte type) {
        return type |= BODY_ZIP_MARK;
    }

    /**
     * 消息体是否压缩
     *
     * @param type 消息类型
     * @return
     */
    static boolean zip(byte type) {
        return (type &= BODY_ZIP_MARK) > 0;
    }

    /**
     * 加上消息体序列化标识
     *
     * @param type          消息类型
     * @param serializeType 序列化类型 Serializer.JSON/PROTO_STUFF
     * @return /
     */
    static byte addSerializeType(byte type, byte serializeType) {
        return type |= (serializeType << 4);
    }

    /**
     * 从消息类型中获取序列化类型
     *
     * @param type 消息类型
     * @return 序列化类型
     */
    static byte getSerializeType(byte type) {
        return (byte) ((type & ProtocolConstant.SERIALIZE_TYPE_MARK) >> 4);
    }

    /**
     * 消息类型字段是否含有某种标识
     *
     * @param type 消息类型
     * @param mark 标识掩码
     * @return /
     */
    static boolean hasState(byte type, byte mark) {
        return (type & mark) > 0;
    }

    /**
     * 消息类型是否是心跳检测请求
     *
     * @param type 消息类型
     * @return true 属于心跳请求包
     */
    static boolean isHeartBeatReq(byte type) {
        return (type & MESSAGE_TYPE_MARK) == HEART_BEAT_REQ;
    }

    /**
     * 消息类型是否是心跳检测响应
     *
     * @param type 消息类型
     * @return true 属于心跳响应包
     */
    static boolean isHeartBeatRes(byte type) {
        return (type & MESSAGE_TYPE_MARK) == HEART_BEAT_RES;
    }


    /**
     * 消息是否是请求
     *
     * @param type 消息类型
     * @return true 请求消息
     */
    static boolean isRequest(byte type) {
        return (type & REQ_RES_TYPE_MARK) == 0;
    }

    /**
     * 消息是否是响应
     *
     * @param type 消息类型
     * @return true 响应消息
     */
    static boolean isResponse(byte type) {
        return !isRequest(type);
    }

    /**
     * 将消息类型转换至响应
     *
     * @param type 原类型
     * @return 响应类型
     */
    static byte transformResponse(byte type) {
        return (byte) ((type |= REQ_RES_TYPE_MARK) & MESSAGE_TYPE_MARK);
    }

    /**
     * 还原成纯消息类型（即后四位）
     *
     * @param type
     * @return
     */
    static byte toOriginMessageType(byte type) {
        return (byte) (type & MESSAGE_TYPE_MARK);
    }


}
