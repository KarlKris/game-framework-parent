package com.echo.common.convert.support;

import com.echo.common.convert.converter.ConditionalConverter;
import com.echo.common.convert.converter.Converter;
import com.echo.common.convert.core.ConverterFactory;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.NumberUtils;

/**
 * 数值类型转换
 * @author: li-yuanwen
 */
final class NumberToNumberConverterFactory implements ConverterFactory<Number, Number>, ConditionalConverter {

    @Override
    public <T extends Number> Converter<Number, T> getConverter(Class<T> targetType) {
        return new NumberToNumber<>(targetType);
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return !sourceType.equals(targetType);
    }

    private static final class NumberToNumber<T extends Number> implements Converter<Number, T> {

        private final Class<T> targetType;

        NumberToNumber(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(Number source) {
            return NumberUtils.convertNumberToTargetClass(source, this.targetType);
        }
    }


}
