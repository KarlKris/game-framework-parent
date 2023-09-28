package com.echo.common.concurrency;

import java.util.concurrent.CompletableFuture;

/**
 * 单线程处理多个不同来源的任务
 *
 * @author li-yuanwen
 * @date 2022/7/14
 */
public interface RunnableLoop extends RunnableLoopGroup {


    /**
     * 判断执行方法的线程是否是RunnableLoop线程
     *
     * @return true 执行线程==RunnableLoop线程
     */
    boolean inRunnableLoop();


    /**
     * 判断给定线程是否是RunnableLoop线程
     *
     * @param thread 给定的线程
     * @return true 给定的线程==RunnableLoop线程
     */
    boolean inRunnableLoop(Thread thread);


    /**
     * 线程终止Future
     *
     * @return 线程终止Future
     */
    CompletableFuture<?> terminationFuture();

}
