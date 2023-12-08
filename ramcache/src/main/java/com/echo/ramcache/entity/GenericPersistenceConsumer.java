package com.echo.ramcache.entity;

import com.echo.common.concurrency.RunnableLoop;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 通用的持久化消费者
 *
 * @author: li-yuanwen
 */
@Slf4j
public class GenericPersistenceConsumer implements PersistenceConsumer, Runnable {

    /**
     * 持久化工具
     **/
    private final DataAccessor dataAccessor;
    /**
     * 待持久化的队列
     **/
    private final Queue<AbstractEntity<?>> queue;
    /**
     * 线程
     **/
    private final RunnableLoop runnableLoop;
    /**
     * 间隔(秒)
     **/
    private final int intervalSecond;

    public GenericPersistenceConsumer(DataAccessor dataAccessor
            , RunnableLoop runnableLoop, int intervalSecond) {
        this.dataAccessor = dataAccessor;
        this.queue = new LinkedBlockingQueue<>();
        ;
        this.runnableLoop = runnableLoop;
        this.intervalSecond = intervalSecond;

        // 开始回写
        startScheduler();
    }

    private void startScheduler() {
        scheduleWithFixedDelay();
    }


    private void scheduleWithFixedDelay() {
        runnableLoop.scheduleWithFixedDelay(this, intervalSecond, intervalSecond, TimeUnit.SECONDS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> T findById(PK id) {
        Optional<AbstractEntity<?>> optional = queue.stream().filter(abstractEntity -> abstractEntity.id == id).findFirst();
        return (T) optional.orElse(null);
    }

    @Override
    public <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> void accept(T entity) {
        if (entity.isDeleteStatus() || entity.commit() || entity.isNewStatus()) {
            this.queue.offer(entity);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <PK extends Comparable<PK> & Serializable, T extends AbstractEntity<PK>> Map<PK, T> findAll() {
        Map<PK, T> map = new HashMap<>(queue.size());
        for (AbstractEntity<?> entity : queue) {
            map.put((PK) entity.id, (T) entity);
        }
        return map;
    }


    @Override
    public void immediateWriteAll() {
        runnableLoop.submit(this);
    }

    @Override
    public void run() {
        int size = queue.size();
        Class<?> entityClass = null;
        for (int i = 0; i < size; i++) {
            AbstractEntity<?> entity = queue.poll();
            if (entity == null) {
                continue;
            }
            entityClass = entity.getClass();
            try {
                if (entity.isDeleteStatus()) {
                    dataAccessor.remove(entity);
                } else if (entity.swap(DataStatus.NEW.getCode(), DataStatus.INIT.getCode())) {
                    dataAccessor.create(entity);
                } else if (entity.swap(DataStatus.MODIFY.getCode(), DataStatus.INIT.getCode())) {
                    dataAccessor.update(entity);
                }
            } catch (Exception e) {
                log.error("持久化发生严重异常, Class:[{}], Entity:[{}]", entity.getClass(), entity, e);
            }
        }
        if (entityClass == null) {
            log.info("write back zero entity");
        } else {
            log.info("write back entity[{}] num: {}", entityClass.getSimpleName(), size);
        }

    }
}
