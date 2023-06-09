package com.li.common.conversion;

import com.li.common.conversion.converter.Converter;

/**
 * 类型转换器注册
 */
public interface ConverterRegistry {


    /**
     * 添加类型转换器
     * @param convertType 转换类型
     * @param converter 类型转换器
     */
    void addConverter(ConvertType convertType, Converter converter);



}
