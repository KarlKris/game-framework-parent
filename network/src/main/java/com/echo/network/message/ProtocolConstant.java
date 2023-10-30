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
     * 8个字节=0 + 1位(消息体是否压缩 0未压缩 1已压缩) + 1位是否携带命令(0不携带命令,1携带命令) + 1位消息类型（0 请求 1 响应） + 4位具体类型
     * 其中消息类型必须确认是否会携带命令
     */

    /**
     * 消息压缩 0 1 0 0 0000
     **/
    byte BODY_ZIP_MARK = 0x40;

    /**
     * 携带命令 0 0 1 0 0000
     **/
    byte COMMAND_MARK = 0x20;

    /**
     * 请求/响应掩码 0 0 0 1 0000
     **/
    byte REQ_RES_TYPE_MARK = 0x10;

    /**
     * 具体消息类型掩码 0 0 0 1 1111
     **/
    byte MESSAGE_TYPE_MARK = 0x1f;


    // 具体消息类型

    // 不携带命令消息类型

    /**
     * 心跳检测请求(不携带命令) 0 0 0 0 0000
     **/
    byte HEART_BEAT_REQ = 0x0;

    /**
     * 心跳检测响应(不携带命令) 0 0 0 1 0000
     **/
    byte HEART_BEAT_RES = 0x10;

    // 携带命令消息类型

    /**
     * 业务请求(携带命令) 0 0 1 0 0001
     **/
    byte BUSINESS_REQ = 0x21;

    /**
     * 业务响应(携带命令) 0 0 1 1 0001
     **/
    byte BUSINESS_RES = 0x31;


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
