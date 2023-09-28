package com.echo.common.concurrency;

import com.li.common.utils.ObjectUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * @author li-yuanwen
 * @date 2022/7/15
 */
public final class ThreadPerTaskExecutor implements Executor {

    private final ThreadFactory threadFactory;

    public ThreadPerTaskExecutor(ThreadFactory threadFactory) {
        this.threadFactory = ObjectUtils.checkNotNull(threadFactory, "threadFactory");
    }

    @Override
    public void execute(Runnable command) {
        threadFactory.newThread(command).start();
    }
}
