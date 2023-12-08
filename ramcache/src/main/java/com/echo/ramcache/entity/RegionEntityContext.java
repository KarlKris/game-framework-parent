package com.echo.ramcache.entity;


import java.io.Serializable;

/**
 * 抽象区域实体容器
 *
 * @author li-yuanwen
 * @date 2022/3/15
 */
public interface RegionEntityContext<PK extends Comparable<PK> & Serializable
        , FK extends Comparable<FK> & Serializable, T extends AbstractRegionEntity<PK, FK>> {

    /**
     * 获取单个实体对象
     *
     * @param id 主键id
     * @return 实体对象
     */
    T findById(PK id);

    /**
     * 添加单个实体
     *
     * @param entity 实体对象
     */
    void add(T entity);

    /**
     * 删除单个实体
     *
     * @param entity 实体对象
     */
    void remove(T entity);

}
