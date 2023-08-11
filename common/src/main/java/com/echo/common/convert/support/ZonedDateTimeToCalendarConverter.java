package com.echo.common.convert.support;

import com.echo.common.convert.converter.Converter;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * ZonedDateTime to Calendar
 * @author: li-yuanwen
 */
final class ZonedDateTimeToCalendarConverter implements Converter<ZonedDateTime, Calendar> {

    @Override
    public Calendar convert(ZonedDateTime source) {
        return GregorianCalendar.from(source);
    }

}
