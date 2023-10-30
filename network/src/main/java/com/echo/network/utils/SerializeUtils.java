package com.echo.network.utils;

import com.echo.network.serialize.JsonSerializer;
import com.echo.network.serialize.ProtoStuffSerializer;
import com.echo.network.serialize.SerializeType;
import com.echo.network.serialize.Serializer;

/**
 * 序列化工具
 *
 * @author: li-yuanwen
 */
public class SerializeUtils {

    static volatile JsonSerializer JSON_SERIALIZER = null;

    public static JsonSerializer getJsonSerializer() {
        if (JSON_SERIALIZER != null) {
            return JSON_SERIALIZER;
        }
        synchronized (SerializeUtils.class) {
            if (JSON_SERIALIZER == null) {
                JSON_SERIALIZER = new JsonSerializer();
            }
        }
        return JSON_SERIALIZER;
    }

    static volatile ProtoStuffSerializer PROTO_STUFF_SERIALIZER = null;

    public static ProtoStuffSerializer getProtoStuffSerializer() {
        if (PROTO_STUFF_SERIALIZER != null) {
            return PROTO_STUFF_SERIALIZER;
        }
        synchronized (SerializeUtils.class) {
            if (PROTO_STUFF_SERIALIZER == null) {
                PROTO_STUFF_SERIALIZER = new ProtoStuffSerializer();
            }
        }
        return PROTO_STUFF_SERIALIZER;
    }

    public static boolean isSupported(byte type) {
        return SerializeType.valueOf(type) != null;
    }

    private static Serializer getSerializerByType(SerializeType serializeType) {
        switch (serializeType) {
            case JSON: {
                return getJsonSerializer();
            }
            case PROTOBUF: {
                return getProtoStuffSerializer();
            }
            default: {
                throw new IllegalArgumentException("not found serialize type single instance : " + serializeType.name());
            }
        }
    }


    public static byte[] serialize(SerializeType serializeType, Object data) {
        Serializer serializer = getSerializerByType(serializeType);
        return serializer.serialize(data);
    }

    public static <T> T deserialize(SerializeType serializeType, byte[] data, Class<T> tClass) {
        Serializer serializer = getSerializerByType(serializeType);
        return serializer.deserialize(data, tClass);
    }


    public static boolean isProtoBuf(byte type) {
        SerializeType serializeType = SerializeType.valueOf(type);
        if (serializeType == null) {
            return false;
        }
        return serializeType == SerializeType.PROTOBUF;
    }

}
