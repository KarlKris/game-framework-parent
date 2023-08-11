package com.echo.common.convert.support;

import com.echo.common.convert.converter.Converter;
import com.echo.common.convert.core.ConverterFactory;
import com.echo.common.util.NumberUtils;

/**
 * 数值型字符串 转换
 * @author: li-yuanwen
 */
final class StringToNumberConverterFactory  implements ConverterFactory<String, Number> {

    @Override
    public <T extends Number> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToNumber<>(targetType);
    }


    private static final class StringToNumber<T extends Number> implements Converter<String, T> {

        private final Class<T> targetType;

        public StringToNumber(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(String source) {
            if (source.isEmpty()) {
                return null;
            }
            return NumberUtils.parseNumber(source, this.targetType);
        }
    }
}
