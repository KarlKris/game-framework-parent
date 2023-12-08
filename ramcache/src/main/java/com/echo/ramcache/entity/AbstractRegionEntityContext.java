package com.echo.ramcache.entity;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 区域实体缓存容器基类,实际进入缓存的对象
 *
 * @author li-yuanwen
 * @date 2022/3/8
 */
public abstract class AbstractRegionEntityContext<PK extends Comparable<PK> & Serializable
        , FK extends Comparable<FK> & Serializable
        , T extends AbstractRegionEntity<PK, FK>> implements RegionEntityContext<PK, FK, T> {


    private final Map<PK, T> cache;

    private final FK owner;

    public AbstractRegionEntityContext(FK owner, List<T> list) {
        this.owner = owner;
        this.cache = new ConcurrentHashMap<>(list.size());
        for (T entity : list) {
            cache.put(entity.getId(), entity);
        }
    }

    /**
     * 获取单个实体对象
     *
     * @param id 主键id
     * @return 实体对象
     */
    @Override
    public T findById(PK id) {
        return cache.get(id);
    }

    /**
     * 添加单个实体至缓存中
     *
     * @param entity 实体对象
     */
    @Override
    public void add(T entity) {
        this.cache.put(entity.getId(), entity);
        afterAddEntity(entity);
    }

    /**
     * 移除缓存中单个实体
     *
     * @param entity 实体对象
     */
    @Override
    public void remove(T entity) {
        this.cache.remove(entity.getId());
        afterRemoveEntity(entity);
    }

    /**
     * 给子类实现的添加单个实体后的逻辑,默认空实现
     *
     * @param entity 实体对象
     */
    protected void afterAddEntity(T entity) {
    }

    /**
     * 给子类实现的移除缓存中单个实体后的逻辑,默认空实现
     *
     * @param entity 实体对象
     */
    protected void afterRemoveEntity(T entity) {
    }

}
