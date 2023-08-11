package com.echo.common.convert.support;

import com.echo.common.convert.converter.Converter;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * ZoneId to TimeZone
 * @author: li-yuanwen
 */
final class ZoneIdToTimeZoneConverter implements Converter<ZoneId, TimeZone> {

    @Override
    public TimeZone convert(ZoneId source) {
        return TimeZone.getTimeZone(source);
    }

}
