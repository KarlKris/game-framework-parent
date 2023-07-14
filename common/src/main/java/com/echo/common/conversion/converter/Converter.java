package com.echo.common.conversion.converter;

import com.echo.common.conversion.ConversionException;
import com.echo.common.conversion.ConversionService;
import com.echo.common.conversion.TypeDescriptor;

/**
 * 类型转换器
 */
public interface Converter {

    /**
     * 转换类型 source-to-target pair
     * @return ConvertiblePair
     */
    ConversionService.ConvertiblePair getConvertiblePair();

    /**
     * 对象类型转换
     * @param source 转换对象
     * @param typeDescriptor 目标类型
     * @return /
     * @throws ConversionException 转换失败时抛出
     */
    Object convert(Object source, TypeDescriptor typeDescriptor) throws ConversionException;


}
