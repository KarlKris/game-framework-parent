package com.echo.common.concurrency;

public interface IdentityRunnable extends Runnable {

    /**
     * 任务标识
     **/
    Object getIdentity();

}
