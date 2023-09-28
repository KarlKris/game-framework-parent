package com.echo.common.concurrency;

/**
 * 任务来源标识接口
 *
 * @author li-yuanwen
 * @date 2022/7/14
 */
public interface RunnableSource {


    /**
     * 是否注册执行线程
     *
     * @return true 已分配了线程
     */
    boolean isRegisterRunnableLoop();


    /**
     * 返回任务来源所对应的线程
     *
     * @return RunnableLoop
     */
    RunnableLoop runnableLoop();


    /**
     * 注册分配来的线程
     *
     * @param runnableLoop 线程
     */
    void register(RunnableLoop runnableLoop);


}
