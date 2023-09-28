package com.echo.common.concurrency;


import com.echo.common.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * 单线程处理任务队列的所有任务
 *
 * @author li-yuanwen
 * @date 2022/7/15
 */
@Slf4j
public class SingleThreadRunnableLoop extends AbstractScheduledRunnableLoop {

    /**
     * 线程未启动
     **/
    private static final int ST_NOT_STARTED = 1;
    /**
     * 线程已启动
     **/
    private static final int ST_STARTED = 2;
    /**
     * 线程正在关闭
     **/
    private static final int ST_SHUTTING_DOWN = 3;
    /**
     * 线程已关闭
     **/
    private static final int ST_SHUTDOWN = 4;
    /**
     * 线程已终止
     **/
    private static final int ST_TERMINATED = 5;

    private static final AtomicIntegerFieldUpdater<SingleThreadRunnableLoop> STATE_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(SingleThreadRunnableLoop.class, "state");

    private volatile int state = ST_NOT_STARTED;

    private final BlockingQueue<Runnable> taskQueue;
    private final Executor executor;

    private volatile Thread thread;

    /**
     * 线程关闭延缓周期时间
     **/
    private volatile long gracefulShutdownQuietPeriod;
    /**
     * 线程关闭最长延缓时间
     **/
    private volatile long gracefulShutdownTimeout;
    /**
     * 线程开始关闭的时间
     **/
    private long gracefulShutdownStartTime;
    /**
     * 上次运行时间
     **/
    private long lastExecutionTime;

    private final CompletableFuture<?> terminationFuture = new CompletableFuture<>();

    SingleThreadRunnableLoop(ThreadFactory threadFactory) {
        this.executor = new ThreadPerTaskExecutor(threadFactory);
        this.taskQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public boolean isShuttingDown() {
        return isShuttingDown(state);
    }

    @Override
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        ObjectUtils.checkPositiveOrZero(quietPeriod, "quietPeriod");
        if (timeout < quietPeriod) {
            throw new IllegalArgumentException(
                    "timeout: " + timeout + " (expected >= quietPeriod (" + quietPeriod + "))");
        }
        ObjectUtils.checkNotNull(unit, "unit");

        if (isShuttingDown()) {
            return terminationFuture;
        }

        boolean wakeup;
        boolean inRunnableLoop = inRunnableLoop();
        int oldState;
        for (; ; ) {
            if (isShuttingDown()) {
                return terminationFuture;
            }
            int newState;
            wakeup = true;
            oldState = state;
            if (inRunnableLoop) {
                newState = ST_SHUTTING_DOWN;
            } else {
                switch (oldState) {
                    case ST_NOT_STARTED:
                    case ST_STARTED:
                        newState = ST_SHUTTING_DOWN;
                        break;
                    default:
                        newState = oldState;
                        wakeup = false;
                }
            }
            if (STATE_UPDATER.compareAndSet(this, oldState, newState)) {
                break;
            }
        }

        gracefulShutdownQuietPeriod = unit.toNanos(quietPeriod);
        gracefulShutdownTimeout = unit.toNanos(timeout);

        if (ensureThreadStarted(oldState)) {
            return terminationFuture;
        }

        if (wakeup) {
            // 唤醒线程
            taskQueue.offer(WAKEUP_TASK);
        }

        return terminationFuture;
    }

    @Override
    public RunnableLoop next() {
        return this;
    }

    @Override
    public void register(RunnableSource source) {
        source.register(this);
    }

    @Override
    public boolean inRunnableLoop() {
        return inRunnableLoop(Thread.currentThread());
    }

    @Override
    public boolean inRunnableLoop(Thread thread) {
        return thread == this.thread;
    }

    @Override
    public void execute(Runnable task) {
        boolean inRunnableLoop = inRunnableLoop();
        addTask(task);
        if (!inRunnableLoop) {
            startThread();
            if (isShutDown()) {
                boolean reject = false;
                try {
                    if (removeTask(task)) {
                        reject = true;
                    }
                } catch (UnsupportedOperationException e) {
                    // 如果任务队列不支持删除,我们需要线程保持正常
                }

                if (reject) {
                    throw new RejectedExecutionException();
                }
            }
        }
    }

    @Override
    public CompletableFuture<?> terminationFuture() {
        return terminationFuture;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private boolean isShuttingDown(int state) {
        return state >= ST_SHUTTING_DOWN;
    }

    private boolean isShutDown() {
        return isShutDown(state);
    }

    private boolean isShutDown(int state) {
        return state >= ST_SHUTDOWN;
    }

    private void addTask(Runnable task) {
        ObjectUtils.checkNotNull(task, "task");
        if (!offerTask(task)) {
            throw new RejectedExecutionException();
        }
    }

    private boolean removeTask(Runnable task) {
        return taskQueue.remove(ObjectUtils.checkNotNull(task, "task"));
    }

    final boolean offerTask(Runnable task) {
        if (isShutDown()) {
            throw new RejectedExecutionException("Runnable Loop terminated");
        }
        return taskQueue.offer(task);
    }

    /**
     * 确保线程已正常启动
     *
     * @param oldState 线程状态
     */
    private boolean ensureThreadStarted(int oldState) {
        if (oldState == ST_NOT_STARTED) {
            try {
                doStartThread();
            } catch (Throwable cause) {
                STATE_UPDATER.set(this, ST_TERMINATED);
                terminationFuture.completeExceptionally(cause);

                return true;
            }
        }
        return false;
    }

    private void startThread() {
        if (state == ST_NOT_STARTED) {
            if (STATE_UPDATER.compareAndSet(SingleThreadRunnableLoop.this, ST_NOT_STARTED, ST_STARTED)) {
                boolean success = false;
                try {
                    doStartThread();
                    success = true;
                } finally {
                    if (!success) {
                        STATE_UPDATER.compareAndSet(SingleThreadRunnableLoop.this, ST_STARTED, ST_NOT_STARTED);
                    }
                }
            }
        }
    }

    /**
     * 启动线程
     */
    private void doStartThread() {
        if (thread != null) {
            return;
        }

        executor.execute(() -> {
            thread = Thread.currentThread();

            boolean success = false;
            lastExecutionTime = nanoTime();
            try {
                // 死循环执行任务,直到线程被设置为shuttingdown状态
                SingleThreadRunnableLoop.this.run();
                success = true;
            } catch (Throwable t) {
                log.warn("Unexpected exception from an RunnableLoop: ", t);
            } finally {
                for (; ; ) {
                    int oldState = state;
                    if (isShuttingDown(oldState)
                            || STATE_UPDATER.compareAndSet(SingleThreadRunnableLoop.this, oldState, ST_SHUTTING_DOWN)) {
                        break;
                    }
                }

                if (success && gracefulShutdownStartTime == 0) {
                    log.error("SingleThreadRunnableLoop.confirmShutdown() 必须在run方法退出之前被调用执行");
                }

                try {
                    // 确保所有的剩余任务执行完,因为现在状态是ST_SHUTTING_DOWN,这依然能接受任务
                    for (; ; ) {
                        if (confirmShutdown()) {
                            break;
                        }
                    }

                    for (; ; ) {
                        int oldState = state;
                        if (isShutDown(oldState)
                                || STATE_UPDATER.compareAndSet(SingleThreadRunnableLoop.this, oldState, ST_SHUTDOWN)) {
                            break;
                        }
                    }

                    // 最后确保一次执行完所有任务
                    confirmShutdown();

                } finally {
                    STATE_UPDATER.set(SingleThreadRunnableLoop.this, ST_TERMINATED);

                    int numTasks = drainTasks();
                    if (numTasks > 0) {
                        log.warn("RunnableLoop线程终止后仍有{}个任务未完成", numTasks);
                    }

                    terminationFuture.complete(null);
                }

            }
        });

    }

    private void run() {
        boolean running = true;
        while (running) {
            try {
                Runnable task = takeTask();
                if (task != null) {
                    safeExecute(task);
                    lastExecutionTime = nanoTime();
                }
            } finally {
                if (isShuttingDown() && confirmShutdown()) {
                    running = false;
                }
            }
        }
    }

    private Runnable takeTask() {
        assert inRunnableLoop();

        for (; ; ) {
            AbstractScheduledRunnableLoop.ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
            if (scheduledTask == null) {
                Runnable task = null;
                try {
                    task = taskQueue.take();
                    if (task == WAKEUP_TASK) {
                        task = null;
                    }
                } catch (InterruptedException e) {
                    // Ignore
                }
                return task;
            } else {
                Runnable task = null;
                long delayNanos = scheduledTask.delayNanos();
                if (delayNanos > 0) {
                    try {
                        task = taskQueue.poll(delayNanos, TimeUnit.NANOSECONDS);
                    } catch (InterruptedException e) {
                        // Waken Up
                        return null;
                    }
                }
                if (task == null) {
                    // 防止taskQueue一直有任务导致scheduledTaskQueue里的任务饿死
                    fetchFromScheduledTaskQueue();
                    task = taskQueue.poll();
                }

                if (task != null) {
                    return task;
                }
            }
        }
    }

    private boolean fetchFromScheduledTaskQueue() {
        if (scheduledTaskQueue == null || scheduledTaskQueue.isEmpty()) {
            return true;
        }
        long nanoTime = nanoTime();
        for (; ; ) {
            Runnable scheduledTask = pollScheduledTask(nanoTime);
            if (scheduledTask == null) {
                return true;
            }

            if (!taskQueue.offer(scheduledTask)) {
                // 入队失败则回到延时队列
                scheduledTaskQueue.add((ScheduledFutureTask<?>) scheduledTask);
                return false;
            }
        }
    }

    private boolean confirmShutdown() {
        if (!isShuttingDown()) {
            return false;
        }

        if (!inRunnableLoop()) {
            throw new IllegalStateException("confirmShutdown() must be invoked from an runnable loop");
        }

        if (gracefulShutdownStartTime == 0) {
            gracefulShutdownStartTime = nanoTime();
        }

        if (runAllTasks()) {
            if (isShutDown()) {
                // 线程已不再接受任务任务
                return true;
            }

            // 此时任务队列仍有可能不为空
            if (gracefulShutdownQuietPeriod == 0) {
                // 无需等待立即终止
                return true;
            }

            return false;
        }

        if (isShutDown()) {
            return true;
        }

        final long nanoTime = nanoTime();

        // 若线程处于ST_SHUTTING_DOWN状态已等待超过gracefulShutdownTimeout时长,则立即终止线程
        if (nanoTime - gracefulShutdownStartTime > gracefulShutdownTimeout) {
            return true;
        }

        //  若线程处于ST_SHUTTING_DOWN状态等待时间未超过gracefulShutdownQuietPeriod时长则暂缓终止线程
        if (nanoTime - lastExecutionTime <= gracefulShutdownQuietPeriod) {
            // 等待100ms
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignore
            }

            return false;
        }

        // 已没有任务执行超过gracefulShutdownQuietPeriod时长,可以安全的终止线程了
        // 但不能保证绝对安全,因为可能此时有任务添加
        return true;
    }

    /**
     * 执行任务队列中的所有任务
     *
     * @return true 至少执行一个任务
     */
    private boolean runAllTasks() {
        if (!inRunnableLoop()) {
            throw new IllegalStateException("runAllTasks() must be invoked from an runnable loop");
        }

        boolean ranAtLeastOne = runAllTasksFrom(taskQueue);

        if (ranAtLeastOne) {
            lastExecutionTime = nanoTime();
        }

        return ranAtLeastOne;
    }

    /**
     * 执行任务队列中的所有方法
     *
     * @param taskQueue 任务队列
     * @return true 至少执行了一个任务
     */
    private boolean runAllTasksFrom(Queue<Runnable> taskQueue) {
        Runnable task = taskQueue.poll();
        if (task == null) {
            return false;
        }

        for (; ; ) {
            safeExecute(task);
            task = taskQueue.poll();
            if (task == null) {
                return true;
            }
        }
    }


    private void safeExecute(Runnable task) {
        try {
            task.run();
        } catch (Throwable t) {
            log.warn("执行任务时出现异常. 任务: {} ", task, t);
        }
    }


    final int drainTasks() {
        int numTasks = 0;
        for (; ; ) {
            Runnable runnable = taskQueue.poll();
            if (runnable == null) {
                break;
            }
            numTasks++;
        }
        return numTasks;
    }


}
