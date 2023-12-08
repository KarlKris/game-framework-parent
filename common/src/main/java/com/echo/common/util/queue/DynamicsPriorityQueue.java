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

    /**
     * 删除所有元素,但是不会调用{@link PriorityQueueNode#priorityQueueIndex(DefaultDynamicsPriorityQueue)}或删除引用
     * 只有当确定节点不会被重新插入到该优先级队列或任何其他优先级队列中，并且已知优先级队列本身在该调用后将被垃圾收集时，才应使用此方法。
     */
    void clearIgnoringIndexes();
}
