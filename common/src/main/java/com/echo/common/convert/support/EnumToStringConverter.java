package com.echo.common.convert.support;

import com.echo.common.convert.converter.Converter;
import com.echo.common.convert.core.ConversionService;

/**
 * Enum to String by {@link Enum#name()}
 * @author: li-yuanwen
 */
public class EnumToStringConverter extends AbstractConditionalEnumConverter implements Converter<Enum<?>, String> {

    public EnumToStringConverter(ConversionService conversionService) {
        super(conversionService);
    }

    @Override
    public String convert(Enum<?> source) {
        return source.name();
    }

}
