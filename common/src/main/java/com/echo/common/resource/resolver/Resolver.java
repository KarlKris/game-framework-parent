package com.echo.common.resource.resolver;

/**
 * 解析器
 * @author li-yuanwen
 * @date 2022/3/22
 */
public interface Resolver {

    /**
     * 解析目标value对象
     * @param obj 解析目标
     * @return 解析结果
     */
    Object resolve(Object obj);

}
