package com.echo.redis.core;

import com.echo.common.util.ObjectUtils;

/**
 * @author: li-yuanwen
 */
public abstract class Converters {


    public static Boolean stringToBoolean(String source) {
        return ObjectUtils.nullSafeEquals("OK", source);
    }

    public static Boolean toBoolean(Long source) {
        return source != null && source == 1L;
    }

}
