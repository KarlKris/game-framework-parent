package com.echo.common.conversion;

/**
 * 转换器规则类型
 */
public enum ConvertType {

    /** JSON STRING-><-OBJECT **/
    JSON,

    /** 自定义规则 STRING->OBJECT **/
    CUSTOMIZE,

    /** 根据对象顺序 OBJECT->STRING **/
    FIELD_INDEX,


    ;
}
