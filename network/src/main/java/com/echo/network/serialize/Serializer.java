package com.echo.network.serialize;


import com.echo.common.exception.SerializeFailException;

/**
 * @author li-yuanwen
 * @date 2021/7/31 14:01
 * 序列化消息体接口
 **/
public interface Serializer {


    /**
     * 序列化方式标识
     *
     * @return 2位代表的序列化方式
     */
    byte getSerializerType();

    /**
     * 序列化对象
     *
     * @param obj 具体对象
     * @param <T> 对象类型
     * @return 二进制数组
     */
    <T> byte[] serialize(T obj) throws SerializeFailException;


    /**
     * 反序列化
     *
     * @param data  二进制数组
     * @param clazz 反序列化类型
     * @param <T>   对象类型
     * @return 反序列化后的对象
     */
    <T> T deserialize(byte[] data, Class<T> clazz) throws SerializeFailException;

}
