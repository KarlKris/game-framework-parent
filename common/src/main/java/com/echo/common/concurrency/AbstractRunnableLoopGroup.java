package com.echo.common.concurrency;


import com.echo.common.util.ObjectUtils;

import java.util.concurrent.*;

/**
 * 为RunnableLoop和RunnableLoopGroup提炼公共函数
 *
 * @author li-yuanwen
 * @date 2022/7/14
 */
public abstract class AbstractRunnableLoopGroup implements RunnableLoopGroup {

    static final long DEFAULT_SHUTDOWN_QUIET_PERIOD = 2;
    static final long DEFAULT_SHUTDOWN_TIMEOUT = 30;

    private static final long START_TIME = System.nanoTime();

    static long nanoTime() {
        return System.nanoTime() - START_TIME;
    }

    @Override
    public Future<?> shutdownGracefully() {
        return shutdownGracefully(DEFAULT_SHUTDOWN_QUIET_PERIOD, DEFAULT_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
    }


    @Override
    public Future<?> submit(Runnable task) {
        RunnableFuture<Object> fTask = newTaskFor(task);
        execute(fTask);
        return fTask;
    }

    @Override
    public <V> Future<V> submit(Callable<V> task) {
        RunnableFuture<V> fTask = newTaskFor(task);
        execute(fTask);
        return fTask;
    }


    private <V> RunnableFuture<V> newTaskFor(Runnable task) {
        ObjectUtils.checkNotNull(task, "task");
        return new FutureTask<V>(task, null);
    }

    private <V> RunnableFuture<V> newTaskFor(Callable<V> task) {
        ObjectUtils.checkNotNull(task, "task");
        return new FutureTask<V>(task);
    }


}
