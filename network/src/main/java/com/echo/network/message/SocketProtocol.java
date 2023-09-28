package com.echo.network.message;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * 协议封装
 *
 * @author li-yuanwen
 * @date 2021/12/10
 */
@Getter
@ToString
public class SocketProtocol {

    /**
     * 模块号
     **/
    private final short module;
    /**
     * 方法标识
     **/
    private final byte methodId;

    public SocketProtocol(short module, byte methodId) {
        this.module = module;
        this.methodId = methodId;
    }

    /**
     * 从ByteBuf读取协议
     *
     * @param in ByteBuf
     * @return 协议
     */
    public static SocketProtocol read(ByteBuf in) {
        return new SocketProtocol(in.readShort(), in.readByte());
    }

    /**
     * 写入协议至ByteBuf
     *
     * @param out ByteBuf
     */
    public void write(ByteBuf out) {
        out.writeShort(module);
        out.writeByte(methodId);
    }

    /**
     * 是否是推送消息
     *
     * @return true 推送消息
     */
    public boolean isPushProtocol() {
        return methodId < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SocketProtocol that = (SocketProtocol) o;
        return module == that.module && methodId == that.methodId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, methodId);
    }
}
