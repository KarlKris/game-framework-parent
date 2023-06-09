package com.li.common.conversion.converter;

import com.li.common.conversion.ConversionException;
import com.li.common.conversion.ConversionService;
import com.li.common.conversion.TypeDescriptor;

/**
 * 类型转换器
 */
public interface Converter {

    /**
     * 转换类型 source-to-target pair
     * @return ConvertiblePair
     */
    ConversionService.ConvertiblePair getConvertibleType();

    /**
     * 对象类型转换
     * @param source 转换对象
     * @param typeDescriptor 目标类型
     * @return /
     * @throws ConversionException 转换失败时抛出
     */
    Object convert(Object source, TypeDescriptor typeDescriptor) throws ConversionException;


}
