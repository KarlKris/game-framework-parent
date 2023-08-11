package com.echo.common.convert.core;

import com.echo.common.convert.converter.Converter;
import com.echo.common.convert.converter.GenericConverter;

/**
 * Converter 注册中心
 */
public interface ConverterRegistry {

    void addConverter(Converter<?, ?> converter);

    <S, T> void addConverter(Class<S> sourceType, Class<T> targetType, Converter<? super S, ? extends T> converter);

    void addConverter(GenericConverter converter);

    void addConverterFactory(ConverterFactory<?, ?> factory);

    void removeConvertible(Class<?> sourceType, Class<?> targetType);
}
