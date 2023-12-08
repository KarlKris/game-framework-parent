package com.echo.ramcache.entity;


import java.io.Serializable;

/**
 * 区域实体缓存层
 *
 * @author li-yuanwen
 * @date 2022/3/8
 */
public interface RegionEntityCacheService {


    /**
     * 从缓存中获取区域缓存,如果没有就从数据库中读取所有数据进行构建
     *
     * @param owner   外键
     * @param tClass  实体类型
     * @param builder 区域缓存构建器
     * @param <PK>    主键类型
     * @param <FK>    外键类型
     * @param <T>     实体类型
     * @param <R>     区域缓存类型
     * @return 区域缓存
     */
    <PK extends Comparable<PK> & Serializable
            , FK extends Comparable<FK> & Serializable
            , T extends AbstractRegionEntity<PK, FK>
            , R extends RegionEntityContext<PK, FK, T>> R loadRegionContext(FK owner, Class<T> tClass, RegionEntityContextBuilder<FK, T, R> builder);


    /**
     * 创建实体并加入到区域缓存中
     *
     * @param entity 实体对象
     * @param <PK>   主键类型
     * @param <FK>   外键类型
     * @param <T>    实体类型
     */
    <PK extends Comparable<PK> & Serializable
            , FK extends Comparable<FK> & Serializable
            , T extends AbstractRegionEntity<PK, FK>> void createRegionEntity(T entity);


    /**
     * 从缓存中移除实体对象,并移除删除数据库数据
     *
     * @param entity 实体对象
     * @param <PK>   主键类型
     * @param <FK>   外键类型
     * @param <T>    实体类型
     */
    <PK extends Comparable<PK> & Serializable
            , FK extends Comparable<FK> & Serializable
            , T extends AbstractRegionEntity<PK, FK>> void remove(T entity);

}
