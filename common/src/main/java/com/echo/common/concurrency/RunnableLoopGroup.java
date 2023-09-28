package com.echo.common.concurrency;

import java.util.concurrent.*;

/**
 * 可分配线程,可为RunnableSource分配线程的线程池
 *
 * @author li-yuanwen
 * @date 2022/7/14
 */
public interface RunnableLoopGroup extends Executor {


    /**
     * 判断线程池状态是否是shut down
     *
     * @return true shut down
     */
    boolean isShuttingDown();


    /**
     * 优雅关闭线程池
     *
     * @return 关闭线程池Future
     */
    Future<?> shutdownGracefully();


    /**
     * 优雅关闭线程池
     *
     * @param quietPeriod
     * @param timeout
     * @param unit
     * @return
     */
    Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit);


    /**
     * 提交任务至线程池
     *
     * @param task 任务
     * @return 任务Future
     */
    Future<?> submit(Runnable task);


    /**
     * 提交有返回值的任务至线程池
     *
     * @param task 任务
     * @param <V>  返回值类型
     * @return 任务Future
     */
    <V> Future<V> submit(Callable<V> task);


    /**
     * 分配一个线程
     *
     * @return 线程
     */
    RunnableLoop next();


    /**
     * 为某个任务来源分配线程
     *
     * @param source source
     */
    void register(RunnableSource source);

    /**
     * 无返回值的延时执行任务
     *
     * @param task  任务
     * @param delay 延时值
     * @param unit  时间单位
     * @return future
     */
    ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit);

    /**
     * 有返回值的延时执行任务
     *
     * @param task  任务
     * @param delay 延时值
     * @param unit  时间单位
     * @param <V>   返回值类型
     * @return 带值的future
     */
    <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit);

    /**
     * 固定频率周期执行任务
     *
     * @param task         任务
     * @param initialDelay 第一次任务的延时值
     * @param period       频率
     * @param unit         时间单位
     * @return future
     */
    ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit);

    /**
     * 固定间隔周期执行任务
     *
     * @param task         任务
     * @param initialDelay 第一次任务的延时值
     * @param delay        间隔
     * @param unit         时间单位
     * @return future
     */
    ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit);

}
