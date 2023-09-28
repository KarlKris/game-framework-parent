package com.echo.common.concurrency;

/**
 * RunnableSource的默认实现
 *
 * @author li-yuanwen
 * @date 2022/7/15
 */
public class DefaultRunnableSource extends AbstractRunnableSource {

    private final Object identity;

    public DefaultRunnableSource(Object identity) {
        this.identity = identity;
    }

    public Object getIdentity() {
        return identity;
    }

}
