package com.echo.ramcache.entity;


import java.io.Serializable;

/**
 * 实体数据缓存层
 *
 * @author li-yuanwen
 * @date 2022/3/8
 */
public interface EntityCacheService {

    /**
     * 从缓存读取实体,若缓存中没有则从数据库中读取
     *
     * @param id     实体主键
     * @param tClass 实体类型
     * @param <PK>   主键类型
     * @param <T>    实体类型
     * @return 实体
     */
    <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> T loadEntity(PK id, Class<T> tClass);


    /**
     * 从缓存读取实体,若缓存中没有则从数据库中读取,若数据库中还没有,则使用EntityBuilder.build()构建
     *
     * @param id            实体主键
     * @param tClass        实体类型
     * @param entityBuilder 实体构建器
     * @param <PK>          主键类型
     * @param <T>           实体类型
     * @return 实体
     */
    <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> T loadOrCreate(PK id, Class<T> tClass
            , EntityBuilder<PK, T> entityBuilder);

    /**
     * 创建实体并加入到缓存中
     *
     * @param entity 创建实体
     * @param <PK>   标识类型
     * @param <T>    实体类型
     * @return 数据库实体
     */
    <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> T createEntity(T entity);

    /**
     * 从缓存中移除实体,并异步数据库中的数据
     *
     * @param entity 待移除的实体
     * @param <PK>   主键类型
     * @param <T>    实体类型
     */
    <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> void remove(T entity);

}
