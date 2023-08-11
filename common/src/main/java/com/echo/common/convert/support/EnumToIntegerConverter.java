package com.echo.common.convert.support;

import com.echo.common.convert.converter.Converter;
import com.echo.common.convert.core.ConversionService;

/**
 * Enum to String by {@link Enum#ordinal()}
 * @author: li-yuanwen
 */
final class EnumToIntegerConverter extends AbstractConditionalEnumConverter implements Converter<Enum<?>, Integer> {

    public EnumToIntegerConverter(ConversionService conversionService) {
        super(conversionService);
    }

    @Override
    public Integer convert(Enum<?> source) {
        return source.ordinal();
    }

}
