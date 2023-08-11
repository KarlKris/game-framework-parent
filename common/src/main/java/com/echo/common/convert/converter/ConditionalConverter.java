package com.echo.common.convert.converter;

import com.echo.common.convert.core.TypeDescriptor;

/**
 * 条件转换器
 * 允许 Converter、GenericConverter 或 ConverterFactory 根据源和目标 TypeDescriptor 的属性有条件地执行。
 * 通常用于根据字段或类级特征（如注释或方法）的存在有选择地匹配自定义转换逻辑。
 * 例如，从字符串字段转换为日期字段时，如果目标字段也已使用 @DateTimeFormat 批注，则实现可能会返回 true。
 * 再举一个例子，从字符串字段转换为帐户字段时，如果目标帐户类定义了公共静态 findAccount（String） 方法，则实现可能会返回 true。
 */
public interface ConditionalConverter {


    /**
     * 是否应该选择当前正在考虑的从源类型到目标类型的转换？
     * @param sourceType sourceType
     * @param targetType targetType
     * @return 能转换 true
     */
    boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType);
}
