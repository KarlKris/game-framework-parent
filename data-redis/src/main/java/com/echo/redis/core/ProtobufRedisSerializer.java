package com.echo.redis.core;

import com.echo.common.exception.SerializeFailException;
import com.echo.common.util.ProtoStuffUtils;

/**
 * @author: li-yuanwen
 */
public class ProtobufRedisSerializer implements RedisSerializer {

    @Override
    public byte[] serialize(Object t) throws SerializeFailException {
        return ProtoStuffUtils.serialize(t);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> tClass) throws SerializeFailException {
        return ProtoStuffUtils.deserialize(bytes, tClass);
    }
}
