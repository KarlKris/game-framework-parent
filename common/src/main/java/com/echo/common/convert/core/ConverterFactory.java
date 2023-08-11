package com.echo.common.convert.core;

import com.echo.common.convert.converter.Converter;

/**
 * 提供从S到R及其子类的转换器 1:N
 * @param <S> S
 * @param <R> R
 */
public interface ConverterFactory<S, R> {


    /**
     * 获取转换器从 S 转换为目标类型 T，其中 T 也是 R 的实例。
     * @param targetType 目标类型
     * @return null T
     * @param <T> T
     */
    <T extends R> Converter<S, T> getConverter(Class<T> targetType);

}
