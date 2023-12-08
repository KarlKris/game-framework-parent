package com.echo.ramcache.entity;

import cn.hutool.core.annotation.AnnotationUtil;
import com.echo.common.concurrency.MultiThreadRunnableLoopGroup;
import com.echo.common.concurrency.RunnableLoopGroup;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * 数据持久化
 *
 * @author: li-yuanwen
 */
public class GenericDataPersistence implements DataPersistence {

    private final DataAccessor accessor;
    /**
     * 持久化线程
     **/
    private final RunnableLoopGroup group;
    /**
     * 实体对应的持久消费者
     **/
    private final ConcurrentHashMap<Class<?>, PersistenceConsumer> consumerMap;

    public GenericDataPersistence(DataAccessor accessor) {
        this.accessor = accessor;
        this.consumerMap = new ConcurrentHashMap<>();
        this.group = new MultiThreadRunnableLoopGroup(Runtime.getRuntime().availableProcessors() + 1);
    }

    private PersistenceConsumer getPersistenceConsumer(Class<?> clazz) {
        PersistenceConsumer consumer = consumerMap.get(clazz);
        if (consumer == null) {
            // 默认间隔5分钟回写
            int second = 300;
            Persisted persisted = AnnotationUtil.getAnnotation(clazz, Persisted.class);
            if (persisted != null) {
                second = persisted.intervalSecond();
            }
            final int intervalSecond = second;
            consumer = consumerMap.computeIfAbsent(clazz, k -> new GenericPersistenceConsumer(accessor, group.next(), intervalSecond));
        }
        return consumer;
    }

    @Override
    public <PK extends Comparable<PK> & Serializable> void commit(AbstractEntity<PK> entity) {
        PersistenceConsumer consumer = getPersistenceConsumer(entity.getClass());
        consumer.accept(entity);
    }

    @Override
    public <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> T findById(PK id, Class<T> tClass) {
        PersistenceConsumer consumer = getPersistenceConsumer(tClass);
        return consumer.findById(id);
    }

    @Override
    public <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> Map<PK, T> findAllByClass(Class<T> tClass) {
        PersistenceConsumer consumer = getPersistenceConsumer(tClass);
        return consumer.findAll();
    }

    @Override
    public Future<?> shutdownGracefully() {
        // 持久化所有任务
        consumerMap.values().forEach(PersistenceConsumer::immediateWriteAll);
        return group.shutdownGracefully();
    }
}
