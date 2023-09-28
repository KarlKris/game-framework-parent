package com.echo.common.concurrency;


import com.echo.common.util.ObjectUtils;
import com.echo.common.util.queue.DefaultDynamicsPriorityQueue;
import com.echo.common.util.queue.DynamicsPriorityQueue;
import com.echo.common.util.queue.PriorityQueueNode;

import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * @author li-yuanwen
 * @date 2022/10/14
 */
public abstract class AbstractScheduledRunnableLoop extends AbstractRunnableLoopGroup implements RunnableLoop {

    private static final Comparator<ScheduledFutureTask<?>> SCHEDULED_FUTURE_TASK_COMPARATOR = ScheduledFutureTask::compareTo;

    // Do nothing
    static final Runnable WAKEUP_TASK = () -> {
    };

    static long deadlineNanos(long delay) {
        long deadlineNanos = nanoTime() + delay;
        // Guard against overflow
        return deadlineNanos < 0 ? Long.MAX_VALUE : deadlineNanos;
    }

    /**
     * 下一个任务唯一Id
     **/
    long nextTaskId;

    /**
     * 延时任务队列
     **/
    DynamicsPriorityQueue<ScheduledFutureTask<?>> scheduledTaskQueue;

    DynamicsPriorityQueue<ScheduledFutureTask<?>> scheduledTaskQueue() {
        if (scheduledTaskQueue == null) {
            scheduledTaskQueue = new DefaultDynamicsPriorityQueue<>(SCHEDULED_FUTURE_TASK_COMPARATOR);
        }
        return scheduledTaskQueue;
    }

    protected final Runnable pollScheduledTask() {
        return pollScheduledTask(nanoTime());
    }

    protected final Runnable pollScheduledTask(long nanoTime) {
        assert inRunnableLoop();
        ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
        if (scheduledTask == null || scheduledTask.deadlineNanos - nanoTime > 0) {
            return null;
        }
        scheduledTaskQueue.remove();
        scheduledTask.setConsumed();
        return scheduledTask;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        ObjectUtils.checkNotNull(task, "task");
        ObjectUtils.checkNotNull(unit, "unit");
        if (delay < 0) {
            delay = 0;
        }

        return schedule(new ScheduledFutureTask<Void>(this, task, deadlineNanos(unit.toNanos(delay))));
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit) {
        ObjectUtils.checkNotNull(task, "task");
        ObjectUtils.checkNotNull(unit, "unit");
        if (delay < 0) {
            delay = 0;
        }
        return schedule(new ScheduledFutureTask<V>(this, task, deadlineNanos(unit.toNanos(delay))));
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        ObjectUtils.checkNotNull(task, "task");
        ObjectUtils.checkNotNull(unit, "unit");
        if (initialDelay < 0) {
            throw new IllegalArgumentException(String.format("initialDelay: %d (expected: >= 0)", initialDelay));
        }
        if (period <= 0) {
            throw new IllegalArgumentException(String.format("period: %d (expected: > 0)", period));
        }
        return schedule(new ScheduledFutureTask<Void>(this, task, deadlineNanos(unit.toNanos(initialDelay)), unit.toNanos(period)));
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        ObjectUtils.checkNotNull(task, "task");
        ObjectUtils.checkNotNull(unit, "unit");
        if (initialDelay < 0) {
            throw new IllegalArgumentException(String.format("initialDelay: %d (expected: >= 0)", initialDelay));
        }
        if (delay <= 0) {
            throw new IllegalArgumentException(String.format("delay: %d (expected: > 0)", delay));
        }
        return schedule(new ScheduledFutureTask<Void>(this, task, deadlineNanos(unit.toNanos(initialDelay)), -unit.toNanos(delay)));
    }

    @Override
    public boolean inRunnableLoop() {
        return inRunnableLoop(Thread.currentThread());
    }

    private <V> ScheduledFuture<V> schedule(final ScheduledFutureTask<V> task) {
        if (inRunnableLoop()) {
            scheduledFromRunnableLoop(task);
        } else {
            execute(task);
        }

        return task;
    }

    final void scheduledFromRunnableLoop(final ScheduledFutureTask<?> task) {
        scheduledTaskQueue().add(task.setId(++nextTaskId));
    }

    /**
     * 获取最快执行的延时任务
     *
     * @return 延时任务 or null
     */
    final ScheduledFutureTask<?> peekScheduledTask() {
        Queue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
        return scheduledTaskQueue != null ? scheduledTaskQueue.peek() : null;
    }

    final void removeScheduledTask(final ScheduledFutureTask<?> task) {
        assert task.isCancelled();
        if (inRunnableLoop()) {
            scheduledTaskQueue().remove(task);
        } else {
            // 唤醒线程去执行任务
            execute(task);
        }
    }


    static long deadlineToDelayNanos(long deadlineNanos) {
        return deadlineNanos == 0L ? 0L : Math.max(0L, deadlineNanos - nanoTime());
    }


    public static final class ScheduledFutureTask<V> extends FutureTask<V> implements ScheduledFuture<V>, PriorityQueueNode {


        /**
         * 任务唯一标识(add队列时设置)
         **/
        private long id;
        /**
         * 执行时间
         **/
        private long deadlineNanos;
        /**
         * 周期
         * 等于0 代表不周期执行
         * 大于0 代表以固定频率执行
         * 小于0 代表以固定间隔执行
         */
        private final long periodNanos;
        /**
         * 执行器
         **/
        private final AbstractScheduledRunnableLoop executor;

        /**
         * 队列索引
         **/
        private int index = PriorityQueueNode.INDEX_NOT_IN_QUEUE;

        ScheduledFutureTask(AbstractScheduledRunnableLoop executor, Runnable runnable, long nanoTime) {
            this(executor, runnable, nanoTime, 0);
        }

        ScheduledFutureTask(AbstractScheduledRunnableLoop executor, Runnable runnable, long nanoTime, long period) {
            super(runnable, null);
            this.executor = executor;
            this.deadlineNanos = nanoTime;
            this.periodNanos = period;
        }

        ScheduledFutureTask(AbstractScheduledRunnableLoop executor, Callable<V> callable, long nanoTime) {
            this(executor, callable, nanoTime, 0);
        }

        ScheduledFutureTask(AbstractScheduledRunnableLoop executor, Callable<V> callable, long nanoTime, long period) {
            super(callable);
            this.executor = executor;
            this.deadlineNanos = nanoTime;
            this.periodNanos = period;
        }


        AbstractScheduledRunnableLoop executor() {
            return executor;
        }

        ScheduledFutureTask<V> setId(long id) {
            if (this.id == 0L) {
                this.id = id;
            }
            return this;
        }

        @Override
        public int priorityQueueIndex(DefaultDynamicsPriorityQueue<?> queue) {
            return index;
        }

        @Override
        public void priorityQueueIndex(DefaultDynamicsPriorityQueue<?> queue, int index) {
            this.index = index;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(delayNanos(), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            if (this == o) {
                return 0;
            }

            ScheduledFutureTask<?> that = (ScheduledFutureTask<?>) o;
            long d = deadlineNanos - that.deadlineNanos;
            if (d < 0) {
                return -1;
            } else if (d > 0) {
                return 1;
            } else if (id < that.id) {
                return -1;
            } else {
                return 1;
            }

        }

        long delayNanos() {
            return deadlineToDelayNanos(deadlineNanos);
        }

        @Override
        public void run() {
            assert executor().inRunnableLoop();
            if (delayNanos() > 0L) {
                if (isCancelled()) {
                    executor().scheduledTaskQueue().remove(this);
                } else {
                    executor().scheduledFromRunnableLoop(this);
                }
                return;
            }
            if (periodNanos == 0L) {
                runTask();
            } else {
                if (runAndResetTask()) {
                    if (!executor().isShuttingDown()) {
                        if (periodNanos > 0L) {
                            deadlineNanos += periodNanos;
                        } else {
                            deadlineNanos = nanoTime() - periodNanos;
                        }
                        if (!isCancelled()) {
                            executor().scheduledTaskQueue().add(this);
                        }
                    }
                }
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean canceled = super.cancel(mayInterruptIfRunning);
            if (canceled) {
                executor().removeScheduledTask(this);
            }
            return canceled;
        }

        void runTask() {
            ScheduledFutureTask.super.run();
        }

        boolean runAndResetTask() {
            return ScheduledFutureTask.super.runAndReset();
        }

        void setConsumed() {
            // 避免多次检查,出队时设置
            if (periodNanos == 0) {
                deadlineNanos = 0L;
            }
        }
    }

}
