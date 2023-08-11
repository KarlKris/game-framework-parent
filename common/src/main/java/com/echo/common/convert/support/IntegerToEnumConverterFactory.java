package com.echo.common.convert.support;

import com.echo.common.convert.converter.ConditionalConverter;
import com.echo.common.convert.converter.Converter;
import com.echo.common.convert.core.ConverterFactory;
import com.echo.common.convert.core.TypeDescriptor;

/**
 * Integer to Enum by {@link Class#getEnumConstants()}
 * @author: li-yuanwen
 */
final class IntegerToEnumConverterFactory implements ConverterFactory<Integer, Enum>, ConditionalConverter {

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return targetType.isEnum();
    }

    @Override
    public <T extends Enum> Converter<Integer, T> getConverter(Class<T> targetType) {
        return new IntegerToEnum(getEnumType(targetType));
    }


    private static class IntegerToEnum<T extends Enum> implements Converter<Integer, T> {

        private final Class<T> enumType;

        public IntegerToEnum(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(Integer source) {
            return this.enumType.getEnumConstants()[source];
        }
    }

    public static Class<?> getEnumType(Class<?> targetType) {
        Class<?> enumType = targetType;
        while (enumType != null && !enumType.isEnum()) {
            enumType = enumType.getSuperclass();
        }
        return enumType;
    }

}
