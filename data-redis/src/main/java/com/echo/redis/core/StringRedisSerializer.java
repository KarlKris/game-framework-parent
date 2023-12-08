package com.echo.redis.core;

import com.echo.common.exception.SerializeFailException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 字符串 序列化/反序列化
 *
 * @author: li-yuanwen
 */
public class StringRedisSerializer implements RedisSerializer {

    public static final StringRedisSerializer UTF_8 = new StringRedisSerializer();

    private final Charset charset;

    public StringRedisSerializer() {
        this(StandardCharsets.UTF_8);
    }

    public StringRedisSerializer(Charset charset) {
        this.charset = charset;
    }

    @Override
    public byte[] serialize(Object t) throws SerializeFailException {
        return ((String) t).getBytes(charset);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> tClass) throws SerializeFailException {
        if (bytes == null || tClass != String.class) {
            return null;
        }
        return (T) new String(bytes, charset);
    }

}
