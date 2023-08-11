package com.echo.common.convert.support;

import com.echo.common.convert.converter.Converter;

/**
 * Number to Character
 * @author: li-yuanwen
 */
final class NumberToCharacterConverter implements Converter<Number, Character> {

    @Override
    public Character convert(Number source) {
        return (char) source.shortValue();
    }

}
