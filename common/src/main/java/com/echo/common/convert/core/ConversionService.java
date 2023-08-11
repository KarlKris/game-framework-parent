package com.echo.common.convert.core;

/**
 * 转换服务
 */
public interface ConversionService {

    boolean canConvert(Class<?> sourceType, Class<?> targetType);

    boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType);

    <T> T convert(Object source, Class<T> targetType);

    Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);

}
