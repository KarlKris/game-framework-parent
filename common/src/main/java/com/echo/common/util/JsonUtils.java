package com.echo.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;

/**
 * Json 工具类
 */
public class JsonUtils {

    public static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();

    /**
     * Jackson ObjectMapper
     **/
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 通过该方法对mapper对象进行设置，所有序列化的对象都将按改规则进行系列化
        // Include.Include.ALWAYS 默认
        // Include.NON_DEFAULT 属性为默认值不序列化
        // Include.NON_EMPTY 属性为 空（""） 或者为 NULL 都不序列化，则返回的json是没有这个字段的。这样对移动端会更省流量
        // Include.NON_NULL 属性为NULL 不序列化
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 允许出现单引号
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        OBJECT_MAPPER.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        OBJECT_MAPPER.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        //表示：在反序列化时，针对哪些目标对象中没有的属性jackson会直接忽略掉，就能反序列化成功
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    /**
     * 将Json字符串转成Object
     *
     * @param json   json
     * @param tClass 目标类型
     * @param <T>
     * @return /
     * @throws JsonProcessingException
     */
    public static <T> T toObj(String json, Class<T> tClass) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(json, tClass);
    }


    /**
     * Json字符串转Object
     *
     * @param content  json
     * @param javaType 目标对象
     * @return obj
     * @throws JsonProcessingException 转对象失败时抛出
     */
    public static Object toObj(String content, JavaType javaType) throws JsonProcessingException {
        if (javaType.isEnumType()) {
            return OBJECT_MAPPER.convertValue(content, javaType);
        }
        return OBJECT_MAPPER.readValue(content, javaType);
    }


    /**
     * 将obj转换成json字符串
     *
     * @param source 对象
     * @return json
     * @throws JsonProcessingException 转json失败时抛出
     */
    public static String toJson(Object source) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(source);
    }


    /**
     * 将obj转化成byte数组
     *
     * @param source 对象
     * @return byte数据
     * @throws JsonProcessingException
     */
    public static byte[] toBytes(Object source) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsBytes(source);
    }

    /**
     * 将byte数组转化成Object
     *
     * @param data 对象
     * @return byte数据
     * @throws JsonProcessingException
     */
    public static <T> T toObj(byte[] data, Class<T> tClass) throws IOException {
        return OBJECT_MAPPER.readValue(data, tClass);
    }
}
