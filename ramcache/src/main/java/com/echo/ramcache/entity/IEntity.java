package com.echo.ramcache.entity;

/**
 * 数据库表对象基类
 *
 * @author li-yuanwen
 */
public interface IEntity<PK> {

    /**
     * 获取主键
     *
     * @return 主键
     */
    PK getId();
}
