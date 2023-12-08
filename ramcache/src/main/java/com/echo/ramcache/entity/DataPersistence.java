package com.echo.ramcache.entity;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * 数据持久化接口
 */
public interface DataPersistence {


    /**
     * 回写到数据库
     *
     * @param entity 回写内容
     * @param <PK>   /
     */
    <PK extends Comparable<PK> & Serializable> void commit(AbstractEntity<PK> entity);


    /**
     * 从待持久化队列中查找指定主键和指定类型的实体数据
     *
     * @param id     主键
     * @param tClass 实体数据
     * @param <PK>   主键类型
     * @param <T>    实体数据实际类型
     * @return null or　对应的实体数据(有可能是待删除的实体,通过entity.isDeleteStatus()判断)
     */
    <PK extends Comparable<PK> & Serializable
            , T extends AbstractEntity<PK>> T findById(PK id, Class<T> tClass);

    /**
     * 查询某个类型的持久化数据集
     *
     * @param tClass 实体数据
     * @param <PK>   主键类型
     * @param <T>    实体数据实际类型
     * @return 持久化数据集
     */
    <PK extends Comparable<PK> & Serializable
            , T extends AbstractEntity<PK>> Map<PK, T> findAllByClass(Class<T> tClass);

    /**
     * 停止持久化
     */
    Future<?> shutdownGracefully();

}
