package com.echo.common.convert.support;

import com.echo.common.convert.converter.Converter;
import com.echo.common.convert.core.ConverterFactory;
import com.echo.common.util.NumberUtils;

/**
 * Character to Number
 * @author: li-yuanwen
 */
final class CharacterToNumberFactory implements ConverterFactory<Character, Number> {

    @Override
    public <T extends Number> Converter<Character, T> getConverter(Class<T> targetType) {
        return new CharacterToNumber<>(targetType);
    }

    private static final class CharacterToNumber<T extends Number> implements Converter<Character, T> {

        private final Class<T> targetType;

        public CharacterToNumber(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(Character source) {
            return NumberUtils.convertNumberToTargetClass((short) source.charValue(), this.targetType);
        }
    }

}
