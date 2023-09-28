package com.echo.common.concurrency;


import com.echo.common.util.ObjectUtils;

/**
 * RunnableSource的抽象
 *
 * @author li-yuanwen
 * @date 2022/7/15
 */
public abstract class AbstractRunnableSource implements RunnableSource {

    private volatile RunnableLoop runnableLoop;

    @Override
    public boolean isRegisterRunnableLoop() {
        return runnableLoop != null;
    }

    @Override
    public RunnableLoop runnableLoop() {
        RunnableLoop runnableLoop = this.runnableLoop;
        if (runnableLoop == null) {
            throw new IllegalStateException("RunnableSource not registered to an runnable loop");
        }
        return runnableLoop;
    }

    @Override
    public void register(RunnableLoop runnableLoop) {
        ObjectUtils.checkNotNull(runnableLoop, "runnableLoop");

        this.runnableLoop = runnableLoop;
    }
}
