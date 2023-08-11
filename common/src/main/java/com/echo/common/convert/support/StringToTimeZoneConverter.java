package com.echo.common.convert.support;

import com.echo.common.convert.converter.Converter;
import com.echo.common.util.StringUtils;

import java.util.TimeZone;

/**
 * String to TimeZone
 * @author: li-yuanwen
 */
final class StringToTimeZoneConverter implements Converter<String, TimeZone> {

    @Override
    public TimeZone convert(String source) {
        return StringUtils.parseTimeZoneString(source);
    }

}