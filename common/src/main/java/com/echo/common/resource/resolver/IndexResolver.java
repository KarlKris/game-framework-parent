package com.echo.common.resource.resolver;

/**
 * 索引解析器---用于解析索引值
 * @author li-yuanwen
 * @date 2022/3/22
 */
public interface IndexResolver extends Resolver {

    /**
     * 获取索引名称
     * @return 索引名称
     */
    String getIndexName();

    /**
     * 索引是否唯一
     * @return true 唯一
     */
    boolean isUnique();

}
