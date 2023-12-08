package com.echo.ramcache.enhance;

/**
 * 增强器
 *
 * @author li-yuanwen
 * @date 2022/3/14
 */
public interface Enhancer {


    /**
     * 增强对象
     *
     * @param obj 须增强的对象
     * @param <T> 对象类型
     * @return 增强后的对象
     */
    <T> T enhance(T obj);


}
