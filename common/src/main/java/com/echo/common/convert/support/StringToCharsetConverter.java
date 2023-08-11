package com.echo.common.convert.support;

import com.echo.common.convert.converter.Converter;

import java.nio.charset.Charset;

/**
 * String to Charset
 * @author: li-yuanwen
 */
final class StringToCharsetConverter implements Converter<String, Charset> {

    @Override
    public Charset convert(String source) {
        return Charset.forName(source);
    }

}
