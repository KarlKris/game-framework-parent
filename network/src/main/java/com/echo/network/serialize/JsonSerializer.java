package com.echo.network.serialize;

import com.echo.common.util.JsonUtils;
import com.echo.network.exception.SerializeFailException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author li-yuanwen
 * @date 2021/8/8 09:33
 * json序列化与反序列化  jackson
 **/
@Slf4j
public class JsonSerializer implements Serializer {


    @Override
    public byte getSerializerType() {
        return SerializeType.JSON.getType();
    }

    @Override
    public <T> byte[] serialize(T obj) throws SerializeFailException {
        try {
            return JsonUtils.toBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化对象[{}]出现未知异常", obj.getClass().getSimpleName(), e);
            throw new SerializeFailException("序列化对象[" + obj.getClass().getSimpleName() + "]出现未知异常", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws SerializeFailException {
        try {
            return JsonUtils.toObj(data, clazz);
        } catch (IOException e) {
            log.error("反序列化对象[{}]出现未知异常", clazz.getSimpleName(), e);
            throw new SerializeFailException("反序列化对象[" + clazz.getSimpleName() + "]出现未知异常", e);
        }
    }
}
