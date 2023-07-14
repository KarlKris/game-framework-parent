package com.echo.common.resource.storage;

/**
 * Resource内容校验
 * @author li-yuanwen
 * @date 2022/3/15
 */
public interface ResourceValidate {

    /**
     * 内容自身是否是合法的,默认为true
     * @return true 合法
     */
    default boolean isValid() {return true;}




}
