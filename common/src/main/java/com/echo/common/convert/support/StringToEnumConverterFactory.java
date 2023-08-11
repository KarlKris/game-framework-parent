package com.echo.common.convert.support;

import com.echo.common.convert.converter.ConditionalConverter;
import com.echo.common.convert.converter.Converter;
import com.echo.common.convert.core.ConverterFactory;
import com.echo.common.convert.core.TypeDescriptor;

/**
 * String to Enum by {@link Enum#valueOf(Class, String)}
 * @author: li-yuanwen
 */
final class StringToEnumConverterFactory implements ConverterFactory<String, Enum>, ConditionalConverter {

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return targetType.isEnum();
    }

    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnum(getEnumType(targetType));
    }


    private static class StringToEnum<T extends Enum> implements Converter<String, T> {

        private final Class<T> enumType;

        StringToEnum(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T convert(String source) {
            if (source.isEmpty()) {
                // It's an empty enum identifier: reset the enum value to null.
                return null;
            }
            return (T) Enum.valueOf(this.enumType, source.trim());
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