package com.echo.common.convert.support;

import com.echo.common.convert.converter.Converter;

/**
 * Object.toString()
 * @author: li-yuanwen
 */
final class ObjectToStringConverter implements Converter<Object, String> {

    @Override
    public String convert(Object source) {
        return source.toString();
    }

}
