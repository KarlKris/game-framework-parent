package com.echo.ramcache.entity;

import com.echo.common.data.Transient;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 抽象的数据库实体,所有写入数据库（Redis除外）的对象都必须继承的类
 *
 * @author li-yuanwen
 * @date 2022/1/25
 */
public abstract class AbstractEntity<PK extends Comparable<PK> & Serializable> implements IEntity<PK> {

    /**
     * 实体主键
     **/
    protected PK id;

    /**
     * 数据状态,不入库
     **/
    @Transient
    protected final AtomicInteger status;

    /**
     * 从db加载数据时,会自动调用无参构造函数,不允许手动调用
     **/
    public AbstractEntity() {
        this.status = new AtomicInteger(DataStatus.INIT.getCode());
    }

    /**
     * 显式手动创建新的对象调用
     **/
    public AbstractEntity(PK id) {
        this.id = id;
        this.status = new AtomicInteger(DataStatus.NEW.getCode());
    }

    @Override
    public PK getId() {
        return id;
    }

    /**
     * 数据更新后调用更新数据状态
     *
     * @return 更新成功
     */
    public boolean commit() {
        return swap(DataStatus.INIT.getCode(), DataStatus.MODIFY.getCode());
    }

    public boolean isDeleteStatus() {
        return this.status.get() == DataStatus.DELETE.getCode();
    }

    public boolean isNewStatus() {
        return this.status.get() == DataStatus.NEW.getCode();
    }

    public boolean swap(int oldStatus, int newStatus) {
        return this.status.compareAndSet(oldStatus, newStatus);
    }

    public void setDeleteStatus() {
        this.status.set(DataStatus.DELETE.getCode());
    }
}
