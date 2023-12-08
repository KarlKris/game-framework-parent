package com.echo.ramcache.entity;

import com.echo.ramcache.anno.Cacheable;
import com.echo.ramcache.anno.CachedEvict;
import com.echo.ramcache.anno.CachedPut;
import com.echo.ramcache.enhance.EnhanceEntity;
import com.echo.ramcache.enhance.Enhancer;
import com.echo.ramcache.enhance.EntityCommitEnhancer;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 实体缓存Service
 *
 * @author: li-yuanwen
 */
@Slf4j
public class GenericEntityCacheService implements EntityCacheService, RegionEntityCacheService {

    private final DataAccessor accessor;
    private final DataPersistence dataPersistence;
    private final Enhancer enhancer;

    public GenericEntityCacheService(DataAccessor accessor, DataPersistence dataPersistence) {
        this.accessor = accessor;
        this.dataPersistence = dataPersistence;
        this.enhancer = new EntityCommitEnhancer(dataPersistence);
    }

    // --------------------- EntityCacheService 实现 ----------------------------------

    @Override
    @Cacheable(name = "#tClass.getName()", key = "#id")
    public <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> T loadEntity(PK id, Class<T> tClass) {
        T entity = dataPersistence.findById(id, tClass);
        if (entity == null) {
            entity = accessor.load(id, tClass);
        } else if (entity.isDeleteStatus()) {
            entity = null;
        }
        return entity == null ? null : wrapIfNecessary(entity);
    }

    @Override
    @Cacheable(name = "#tClass.getName()", key = "#id")
    public <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> T loadOrCreate(PK id, Class<T> tClass, EntityBuilder<PK, T> entityBuilder) {
        T entity = loadEntity(id, tClass);
        if (entity == null) {
            T newInstance = entityBuilder.build(id);
            dataPersistence.commit(newInstance);
            entity = newInstance;
            return wrapIfNecessary(entity);
        }
        return entity;
    }

    @Override
    @CachedPut(name = "#entity.getClass().getName()", key = "#entity.getId()")
    public <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> T createEntity(T entity) {
        T originEntity = unwrapIfNecessary(entity);
        dataPersistence.commit(originEntity);
        return wrapIfNecessary(entity);
    }

    @Override
    @CachedEvict(name = "#entity.getClass().getName()", key = "#entity.getId()")
    public <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> void remove(T entity) {
        T originEntity = unwrapIfNecessary(entity);
        originEntity.setDeleteStatus();
        dataPersistence.commit(originEntity);
    }

    // --------------------- RegionEntityCacheService 实现 ----------------------------------

    @Override
    @Cacheable(name = "#tClass.getName()", key = "#owner")
    public <PK extends Comparable<PK> & Serializable, FK extends Comparable<FK> & Serializable, T extends AbstractRegionEntity<PK, FK>, R extends RegionEntityContext<PK, FK, T>> R loadRegionContext(FK owner, Class<T> tClass, RegionEntityContextBuilder<FK, T, R> builder) {
        // 先查询待持久化数据集
        Map<PK, T> map = dataPersistence.findAllByClass(tClass);
        // 再查询数据库
        Collection<T> queryResult = accessor.list(owner, tClass);
        List<T> list = new ArrayList<>(queryResult.size());
        for (T t : queryResult) {
            // 获取持久化列表中的数据并替换
            T entity = map.remove(t.getId());
            // 数据待删除
            if (entity != null && entity.isDeleteStatus()) {
                continue;
            }
            // 增强
            list.add(wrapIfNecessary(entity != null ? entity : t));
        }

        if (!map.isEmpty()) {
            map.values().stream()
                    .filter(t -> !t.isDeleteStatus() && t.getOwner() == owner)
                    .map(this::wrapIfNecessary)
                    .forEach(list::add);
        }

        return builder.build(owner, list);
    }

    @Override
    @CachedPut(name = "#entity.getClass().getName()", key = "#entity.getOwner()")
    public <PK extends Comparable<PK> & Serializable, FK extends Comparable<FK> & Serializable, T extends AbstractRegionEntity<PK, FK>> void createRegionEntity(T entity) {
        T originEntity = unwrapIfNecessary(entity);
        dataPersistence.commit(originEntity);
    }

    @Override
    @CachedEvict(name = "#entity.getClass().getName()", key = "#entity.getOwner()")
    public <PK extends Comparable<PK> & Serializable, FK extends Comparable<FK> & Serializable, T extends AbstractRegionEntity<PK, FK>> void remove(T entity) {
        AbstractEntity<PK> originEntity = unwrapIfNecessary(entity);
        originEntity.setDeleteStatus();
        dataPersistence.commit(originEntity);
    }


    /**
     * 去包装得到原始实体数据
     *
     * @param entity 增强实体数据
     * @param <PK>   主键类型
     * @param <T>    实体类型
     * @return 原始实体数据
     */
    @SuppressWarnings("unchecked")
    private <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> T unwrapIfNecessary(T entity) {
        if (entity instanceof EnhanceEntity) {
            return (T) ((EnhanceEntity<PK>) entity).getEntity();
        }
        return entity;
    }

    /**
     * 将原始实体数据包装增强
     *
     * @param entity 原始实体数据
     * @param <PK>   主键类型
     * @param <T>    实体类型
     * @return 增强实体数据
     */
    private <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> T wrapIfNecessary(T entity) {
        if (entity instanceof EnhanceEntity) {
            return entity;
        }
        return enhancer.enhance(entity);
    }
}
