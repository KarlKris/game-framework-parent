package com.echo.redis.core;

import com.echo.common.exception.SerializeFailException;

/**
 * redis key-value 序列化
 */
public interface RedisSerializer {


    /**
     * 序列化对象
     *
     * @param t
     * @return
     * @throws SerializeFailException
     */
    byte[] serialize(Object t) throws SerializeFailException;


    /**
     * 反序列化对象
     *
     * @param bytes
     * @param <T>
     * @return
     * @throws SerializeFailException
     */
    <T> T deserialize(byte[] bytes, Class<T> tClass) throws SerializeFailException;

}
