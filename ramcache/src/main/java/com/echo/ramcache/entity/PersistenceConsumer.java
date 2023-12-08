package com.echo.ramcache.entity;

import java.io.Serializable;
import java.util.Map;

/**
 * 持久化消费者
 */
public interface PersistenceConsumer {


    /**
     * 从待持久化队列中查找指定主键和指定类型的实体数据
     *
     * @param id     主键
     * @param tClass 实体数据
     * @param <PK>   主键类型
     * @param <T>    实体数据实际类型
     * @return null or　对应的实体数据
     */
    <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> T findById(PK id);


    /**
     * 向消费器提交需持久化的实体
     *
     * @param entity 实体数据
     */
    <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> void accept(T entity);


    /**
     * 查询某个类型的持久化数据集
     *
     * @param <PK> 主键类型
     * @param <T>  实体数据实际类型
     * @return 持久化数据集
     */
    <PK extends Comparable<PK> & Serializable
            , T extends AbstractEntity<PK>> Map<PK, T> findAll();

    /**
     * 立即回写数据
     */
    void immediateWriteAll();

}
