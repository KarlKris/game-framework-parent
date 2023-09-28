package com.echo.network.utils;

import cn.hutool.core.util.ClassUtil;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.Data;
import net.webby.protostuff.runtime.Generators;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ProtoStuff 序列化框架工具类
 *
 * @author li-yuanwen
 * @date 2021/7/31 18:40
 **/
public class ProtoStuffUtils {

    /**
     * 避免每次序列化都重新申请Buffer空间
     **/
    private static final ThreadLocal<LinkedBuffer> BUFFER = new ThreadLocal<>();

    /**
     * 缓存Schema
     **/
    private static Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();


    /**
     * 序列化方法，把指定对象序列化成字节数组
     *
     * @param obj
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        LinkedBuffer buffer = BUFFER.get();
        if (buffer == null) {
            buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
            BUFFER.set(buffer);
        }
        byte[] data;
        Object target = obj;
        Schema schema = null;

        Class<T> clazz = (Class<T>) obj.getClass();
        if (isWrapClass(clazz)) {
            schema = getSchema(ProtoStuffWrapper.class);
            ProtoStuffWrapper<T> wrapper = new ProtoStuffWrapper<>();
            wrapper.data = obj;
            target = wrapper;
        } else {
            schema = getSchema(clazz);
        }
        try {
            data = ProtostuffIOUtil.toByteArray(target, schema, buffer);
        } finally {
            buffer.clear();
        }

        return data;
    }

    /**
     * 反序列化方法，将字节数组反序列化成指定Class类型
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] data, Class<T> clazz) {
        if (isWrapClass(clazz)) {
            Schema<ProtoStuffWrapper> schema = getSchema(ProtoStuffWrapper.class);
            ProtoStuffWrapper<T> wrapper = schema.newMessage();
            ProtostuffIOUtil.mergeFrom(data, wrapper, schema);
            return wrapper.data;
        }
        Schema<T> schema = getSchema(clazz);
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, obj, schema);
        return obj;
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) schemaCache.get(clazz);
        if (Objects.isNull(schema)) {
            //这个schema通过RuntimeSchema进行懒创建并缓存
            //所以可以一直调用RuntimeSchema.getSchema(),这个方法是线程安全的
            schema = RuntimeSchema.createFrom(clazz);
            schemaCache.put(clazz, schema);
        }

        return schema;
    }

    /**
     * 生成.proto文件字符串
     *
     * @param clazz java类
     * @param <T>   /
     * @return .proto文件字符串
     */
    public static <T> String toProto(Class<T> clazz) {
        Schema<T> schema = getSchema(clazz);
        return Generators.newProtoGenerator(schema).generate();
    }

    /**
     * 判断是否需要使用ProtoStuffWrapper封装
     *
     * @param clazz 类型
     * @param <T>
     * @return true 是
     */
    public static <T> boolean isWrapClass(Class<T> clazz) {
        // protostuff无法直接序列化/反序列化的类型
        return clazz.isArray()
                || clazz.isEnum()
                || ClassUtil.isAssignable(Collection.class, clazz)
                || ClassUtil.isAssignable(Map.class, clazz);
    }


    /**
     * 序列化/反序列化对象包装类
     * 专为基于 Protostuff 进行序列化/反序列化而定义。
     * Protostuff 是基于POJO进行序列化和反序列化操作。
     * 如果需要进行序列化/反序列化的对象不知道其类型，不能进行序列化/反序列化；
     * 比如Map、List、Set、Enum等是不能进行正确的序列化/反序列化。
     * 因此需要映入一个包装类，把这些需要序列化/反序列化的对象放到这个包装类中。
     * 这样每次 Protostuff 都是对这个类进行序列化/反序列化,不会出现不能/不正常的操作出现
     */
    @Data
    private static class ProtoStuffWrapper<T> {

        /**
         * 实际对象
         **/
        private T data;

    }

}
