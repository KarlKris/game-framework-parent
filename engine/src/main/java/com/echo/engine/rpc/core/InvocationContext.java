package com.echo.engine.rpc.core;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 远程调用消息容器
 *
 * @author li-yuanwen
 * @date 2021/12/10
 */
public class InvocationContext {

    /**
     * 消息序号生成器
     **/
    private final AtomicLong snGenerator = new AtomicLong(0);

    /**
     * 消息序号回复Session
     **/
    private final ConcurrentHashMap<Long, Invocation> invocationHolder = new ConcurrentHashMap<>();

    /**
     * 获取下一个消息序号
     **/
    public long nextSn() {
        return this.snGenerator.incrementAndGet();
    }

    /**
     * 添加远程调用消息Invocation
     *
     * @param invocation 远程调用消息Invocation
     */
    public void addInvocation(Invocation invocation) {
        this.invocationHolder.put(invocation.getSn(), invocation);
    }

    /**
     * 移除远程调用消息Future
     *
     * @param sn 远程调用消息Future序号
     * @return 远程调用消息Future or null
     */
    public Invocation removeSocketFuture(long sn) {
        return this.invocationHolder.remove(sn);
    }

}
