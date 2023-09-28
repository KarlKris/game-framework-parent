package com.echo.common.util.queue;

/**
 * 优先级队列DefaultPriorityQueue内部持有元素
 * 所有方法都应该只在DefaultPriorityQueue内使用
 *
 * @author li-yuanwen
 * @date 2022/10/13
 */
public interface PriorityQueueNode {

    /**
     * 表明元素不在队列内
     **/
    int INDEX_NOT_IN_QUEUE = -1;

    /**
     * 获取上次被#priorityQueueIndex(DefaultPriorityQueue, int)设定的值
     *
     * @return 索引值
     */
    int priorityQueueIndex(DefaultDynamicsPriorityQueue<?> queue);


    /**
     * 设定元素顺序值
     *
     * @param index 索引值
     */
    void priorityQueueIndex(DefaultDynamicsPriorityQueue<?> queue, int index);

}
