package com.echo.ramcache.entity;


import java.io.Serializable;
import java.util.List;

/**
 * @author li-yuanwen
 * @date 2022/3/8
 */
public interface RegionEntityContextBuilder<FK extends Comparable<FK> & Serializable
        , T extends AbstractRegionEntity<?, FK>
        , R extends RegionEntityContext<?, FK, T>> {


    /**
     * 区域缓存构建器
     *
     * @param owner 持有者id
     * @param list  实体集
     * @return 区域缓存
     */
    R build(FK owner, List<T> list);

}
