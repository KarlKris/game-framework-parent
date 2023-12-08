package com.echo.ramcache.enhance;

/**
 * Javassist增强常量
 *
 * @author li-yuanwen
 */
public interface EnhanceConstants {

    /**
     * 增强类后缀
     **/
    String ENHANCE_SUFFIX = "$EnhanceByJavassist";

    /**
     * 增强类数据库回写服务域名
     **/
    String PERSISTENCE_FIELD = "persistence";

    /**
     * 增强类实际对象域名
     **/
    String ENTITY_FIELD = "entity";

    /**
     * 增强类接口
     **/
    String METHOD_GET_ENTITY = "getEntity";

}
