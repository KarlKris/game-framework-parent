package com.echo.ramcache.core;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author li-yuanwen
 * 缓存数据统计
 */
public class CacheStat {

    /**
     * 查询缓存次数
     **/
    private final AtomicInteger queryNum = new AtomicInteger(0);
    /**
     * 命中次数
     **/
    private final AtomicInteger hitNum = new AtomicInteger(0);

    void incrementQuery() {
        this.queryNum.incrementAndGet();
    }

    void incrementHit() {
        this.hitNum.incrementAndGet();
    }

    public int getQueryNum() {
        return queryNum.get();
    }

    public int getHitNum() {
        return hitNum.get();
    }

    public double getRate() {
        return ((double) getQueryNum()) / getHitNum();
    }

}
