package com.echo.common.concurrency;

import cn.hutool.core.thread.NamedThreadFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 根据标识来分配线程执行,实现同一标识顺序执行任务
 *
 * @author: li-yuanwen
 */
public class IdentityRunnableLoopGroup implements Executor {

    /**
     * 30分钟
     **/
    static final int SLEEP_SECOND = 30 * 60 * 1000;

    /**
     * 线程池
     **/
    private final RunnableLoopGroup group;

    private final ConcurrentHashMap<Object, TimeRunnableSource> identityExecutors = new ConcurrentHashMap<>();

    public IdentityRunnableLoopGroup() {
        this(Runtime.getRuntime().availableProcessors() << 1);
    }

    public IdentityRunnableLoopGroup(int threadNum) {
        this(new MultiThreadRunnableLoopGroup(threadNum, new NamedThreadFactory("Identity-Thread", false)));
    }

    public IdentityRunnableLoopGroup(RunnableLoopGroup group) {
        this(group, true);
    }

    public IdentityRunnableLoopGroup(RunnableLoopGroup group, boolean scheduleClear) {
        this.group = group;
        if (scheduleClear) {
            // 2小时清理一次
            this.group.scheduleAtFixedRate(this::clear, 2 * 60 * 60, 2 * 60 * 60, TimeUnit.SECONDS);
        }
    }


    @Override
    public void execute(Runnable command) {
        if (command instanceof IdentityRunnable) {
            execute((IdentityRunnable) command);
        } else {
            group.next().execute(command);
        }
    }

    public void execute(IdentityRunnable runnable) {
        getExecutor(runnable.getIdentity()).execute(runnable);
    }

    public Executor getExecutor(Object identity) {
        RunnableSource runnableSource = identityExecutors.computeIfAbsent(identity, TimeRunnableSource::new);
        if (!runnableSource.isRegisterRunnableLoop()) {
            synchronized (runnableSource) {
                if (!runnableSource.isRegisterRunnableLoop()) {
                    runnableSource.register(group.next());
                }
            }
        }
        return runnableSource.runnableLoop();
    }

    public Executor next() {
        return group.next();
    }

    public Future<?> shutdownGracefully() {
        return group.shutdownGracefully();
    }

    public void clear() {
        long now = System.currentTimeMillis();
        for (Map.Entry<Object, TimeRunnableSource> entry : identityExecutors.entrySet()) {
            if (now - entry.getValue().getTime() < SLEEP_SECOND) {
                continue;
            }
            identityExecutors.remove(entry.getKey());
        }
    }

}
