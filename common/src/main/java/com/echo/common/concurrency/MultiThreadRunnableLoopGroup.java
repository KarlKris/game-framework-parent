package com.echo.common.concurrency;

import cn.hutool.core.thread.NamedThreadFactory;
import com.echo.common.util.ObjectUtils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * 固定线程数量的线程池
 *
 * @author li-yuanwen
 * @date 2022/7/15
 */
public class MultiThreadRunnableLoopGroup extends AbstractRunnableLoopGroup {

    static final int DEFAULT_RUNNABLE_LOOP_THREADS = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * 线程容器
     **/
    private final RunnableLoop[] children;
    /**
     * 终止的线程计数器
     **/
    private final AtomicInteger terminatedChildren = new AtomicInteger();
    private final CompletableFuture<?> terminationFuture = new CompletableFuture<>();
    private final RunnableLoopChooserFactory.RunnableLoopChooser chooser;

    public MultiThreadRunnableLoopGroup() {
        this(0);
    }

    public MultiThreadRunnableLoopGroup(int nThreads) {
        this(nThreads == 0 ? DEFAULT_RUNNABLE_LOOP_THREADS : nThreads, null);
    }

    public MultiThreadRunnableLoopGroup(int nThreads, ThreadFactory threadFactory) {
        ObjectUtils.checkPositive(nThreads, "nThreads");

        if (threadFactory == null) {
            threadFactory = newDefaultThreadFactory();
        }

        children = new RunnableLoop[nThreads];
        for (int i = 0; i < nThreads; i++) {
            boolean success = false;
            try {
                children[i] = newChild(threadFactory);
                success = true;
            } catch (Exception e) {
                throw new IllegalStateException("failed to create a child runnable loop", e);
            } finally {
                if (!success) {
                    for (int j = 0; j < i; j++) {
                        children[j].shutdownGracefully();
                    }
                }
            }
        }

        this.chooser = DefaultRunnableLoopChooserFactory.INSTANCE.newChooser(children);

        BiConsumer<Object, Throwable> terminationListener = (o, throwable) -> {
            if (terminatedChildren.incrementAndGet() == children.length) {
                terminationFuture.complete(null);
            }
        };

        for (RunnableLoop loop : children) {
            loop.terminationFuture().whenComplete(terminationListener);
        }

    }

    @Override
    public boolean isShuttingDown() {
        for (RunnableLoop loop : children) {
            if (!loop.isShuttingDown()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        for (RunnableLoop loop : children) {
            loop.shutdownGracefully(quietPeriod, timeout, unit);
        }
        return terminationFuture;
    }

    @Override
    public RunnableLoop next() {
        return chooser.next();
    }

    @Override
    public void register(RunnableSource source) {
        next().register(source);
    }

    @Override
    public void execute(Runnable command) {
        next().execute(command);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return next().schedule(task, delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit) {
        return next().schedule(task, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return next().scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        return next().scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }


    // -----------------------------------------------------------------------------------------------------------------

    protected ThreadFactory newDefaultThreadFactory() {
        return new NamedThreadFactory("RunnableLoop-", false);
    }

    protected RunnableLoop newChild(ThreadFactory threadFactory) {
        return new SingleThreadRunnableLoop(threadFactory);
    }

}
