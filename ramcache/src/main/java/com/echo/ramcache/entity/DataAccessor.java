package com.echo.ramcache.entity;

import java.io.Serializable;
import java.util.Collection;

/**
 * 数据库访问接口
 */
public interface DataAccessor {

    /**
     * 读取数据库表数据
     *
     * @param id     主键id
     * @param tClass 实体对象class
     * @param <PK>   主键
     * @param <T>    实体类型
     * @return 数据库表某行数据对应实体对象
     */
    <PK extends Comparable<PK> & Serializable, T extends IEntity<PK>> T load(PK id, Class<T> tClass);

    /**
     * 移除数据库表数据
     *
     * @param entity 需要移除的实体
     */
    void remove(AbstractEntity<?> entity);

    /**
     * 更新数据库表数据
     *
     * @param entity 需要更新的实体
     */
    void update(AbstractEntity<?> entity);

    /**
     * 创建数据库表数据
     *
     * @param entity 新创建的实体
     */
    void create(AbstractEntity<?> entity);


    /**
     * 读取数据库表数据,用于一对多的关系模型
     *
     * @param owner  AbstractRegionEntity.getOwner()
     * @param tClass
     * @param <FK>
     * @param <T>
     * @return
     */
    <FK extends Comparable<FK> & Serializable, T extends IEntity<?>> Collection<T> list(FK owner, Class<T> tClass);

}
