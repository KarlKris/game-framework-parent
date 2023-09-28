package com.echo.common.concurrency;


/**
 * @author: li-yuanwen
 */
public class TimeRunnableSource extends DefaultRunnableSource {

    /**
     * 最近getRunnableLoop时间
     **/
    private long time;

    public TimeRunnableSource(Object identity) {
        super(identity);
    }

    @Override
    public RunnableLoop runnableLoop() {
        time = System.currentTimeMillis();
        return super.runnableLoop();
    }

    public long getTime() {
        return time;
    }
}
