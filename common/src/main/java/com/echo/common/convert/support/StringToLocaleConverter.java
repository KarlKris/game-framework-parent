package com.echo.common.convert.support;

import com.echo.common.convert.converter.Converter;
import com.echo.common.util.StringUtils;

import java.util.Locale;

/**
 * String to Locale
 * @author: li-yuanwen
 */
final class StringToLocaleConverter implements Converter<String, Locale> {

    @Override
    public Locale convert(String source) {
        return StringUtils.parseLocale(source);
    }

}
