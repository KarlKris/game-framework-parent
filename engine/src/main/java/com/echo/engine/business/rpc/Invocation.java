package com.echo.engine.business.rpc;


import com.echo.network.message.InnerMessage;

/**
 * 远程调用Invocation
 *
 * @author li-yuanwen
 * @date 2021/12/10
 */
public abstract class Invocation {

    /**
     * 消息序号
     **/
    private final long sn;
    /**
     * 执行rpc的线程所在处理的请求消息序号,可能为null
     **/
    private final Long parentSn;
    /**
     * 请求方唯一标识
     **/
    private final long identity;
    /**
     * 是否是同步调用
     **/
    private final boolean sync;

    public Invocation(long sn, Long outerSn, long identity, boolean sync) {
        this.sn = sn;
        this.parentSn = outerSn;
        this.identity = identity;
        this.sync = sync;
    }

    public long getSn() {
        return sn;
    }

    public Long getParentSn() {
        return parentSn;
    }

    public long getIdentity() {
        return identity;
    }

    public boolean isSync() {
        return sync;
    }

    /**
     * 同步消息结果
     *
     * @param message 消息
     */
    public abstract void complete(InnerMessage message);
}
