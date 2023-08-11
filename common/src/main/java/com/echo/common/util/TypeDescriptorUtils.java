package com.echo.common.util;

import cn.hutool.core.lang.Pair;
import com.echo.common.convert.core.TypeDescriptor;
import org.slf4j.helpers.MessageFormatter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * TypeDescriptor 工具类
 * @author li-yuanwen
 * @date 2022/3/27
 */
public class TypeDescriptorUtils {

    public static TypeDescriptor STRING_DESCRIPTOR = TypeDescriptor.valueOf(String.class);

    public static TypeDescriptor OBJECT = TypeDescriptor.valueOf(Object.class);

    /**
     * 根据Field解析TypeDescriptor
     * @param field 属性
     * @return TypeDescriptor
     */
    public static TypeDescriptor newInstance(Field field) {
        TypeDescriptor descriptor;
        if (ObjectUtils.isMap(field.getType())) {
            Pair<TypeDescriptor, TypeDescriptor> pair = parseKeyValueTypeDescriptor(field);
            descriptor = TypeDescriptor.map(field.getType(), pair.getKey(), pair.getValue());
        } else if (ObjectUtils.isCollection(field.getType())){
            descriptor = TypeDescriptor.collection(field.getType(), parseCollectionNestedTypeDescriptor(field));
        } else {
            descriptor = new TypeDescriptor(field);
        }
        return descriptor;
    }

    /**
     * todo 未考虑嵌套Map或Collection情况
     * 已知属性类型是Map,解析Key和Value的TypeDescriptor
     * @param field Map属性
     * @return key:TypeDescriptor  value:TypeDescriptor
     */
    private static Pair<TypeDescriptor, TypeDescriptor> parseKeyValueTypeDescriptor(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            Class<?> keyClz = null;
            Class<?> valueClz = null;
            if (actualTypeArguments[0] instanceof Class) {
                keyClz = (Class<?>) actualTypeArguments[0];

            } else if (actualTypeArguments[0] instanceof ParameterizedType) {
                keyClz = (Class<?>)((ParameterizedType) actualTypeArguments[0]).getRawType();
            }

            if (actualTypeArguments[1] instanceof Class) {
                valueClz = (Class<?>) actualTypeArguments[1];

            } else if (actualTypeArguments[1] instanceof ParameterizedType) {
                valueClz = (Class<?>)((ParameterizedType) actualTypeArguments[1]).getRawType();
            }

            if (keyClz != null && valueClz != null) {
                return new Pair<>(TypeDescriptor.valueOf(keyClz), TypeDescriptor.valueOf(valueClz));
            }
        }

        String message = MessageFormatter.format("属性[{}]不属于Map", field.getName()).getMessage();
        throw new IllegalArgumentException(message);
    }

    /**
     * todo 未考虑嵌套Map或Collection情况
     * 已知field是Collection，解析泛型类型TypeDescriptor
     * @param field Collection属性
     * @return 泛型类型TypeDescriptor
     */
    private static TypeDescriptor parseCollectionNestedTypeDescriptor(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            Class<?> clz = null;
            if (actualTypeArguments[0] instanceof Class) {
                clz = (Class<?>) actualTypeArguments[0];

            } else if (actualTypeArguments[0] instanceof ParameterizedType) {
                clz = (Class<?>)((ParameterizedType) actualTypeArguments[0]).getRawType();
            }

            if (clz != null) {
                return TypeDescriptor.valueOf(clz);
            }
        }

        String message = MessageFormatter.format("属性[{}]不属于Collection", field.getName()).getMessage();
        throw new IllegalArgumentException(message);
    }

}
