package com.echo.ramcache.entity;

import java.io.Serializable;

/**
 * @author li-yuanwen
 */
public interface EntityBuilder<PK extends Comparable<PK> & Serializable, T extends IEntity<PK>> {

    /**
     * 创建实体
     *
     * @param id 实体标识
     * @return 新实体
     */
    T build(PK id);
}
