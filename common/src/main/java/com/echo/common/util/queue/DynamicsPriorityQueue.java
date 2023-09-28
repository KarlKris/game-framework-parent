package com.echo.common.util.queue;

import java.util.Queue;

/**
 * 支持动态修改元素优先级的优先级队列(非线程安全),基于netty PriorityQueue实现
 *
 * @author li-yuanwen
 * @date 2022/10/13
 */
public interface DynamicsPriorityQueue<T> extends Queue<T> {


    /**
     * 通知队列，元素优先级发生变更
     * 队列会自动调整顺序,以保持队列特性
     *
     * @param node 元素
     */
    void priorityChanged(T node);

}
