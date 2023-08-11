package com.echo.common.util;

import java.util.function.Consumer;

/**
 * 简单的持有value的Consumer
 * @author: li-yuanwen
 */
class Sink<T> implements Consumer<T> {

    private T value;

    /**
     * Returns the value captured.
     *
     * @return
     */
    public T getValue() {
        return value;
    }

    @Override
    public void accept(T t) {
        this.value = t;
    }
}
