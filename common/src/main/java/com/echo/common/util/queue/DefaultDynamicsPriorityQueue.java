package com.echo.common.util.queue;


import com.echo.common.util.ObjectUtils;

import java.util.*;

/**
 * PriorityQueue的默认实现----最小堆
 *
 * @author li-yuanwen
 * @date 2022/10/13
 */
public class DefaultDynamicsPriorityQueue<T extends PriorityQueueNode> extends AbstractQueue<T> implements DynamicsPriorityQueue<T> {

    private static final PriorityQueueNode[] EMPTY_ARRAY = new PriorityQueueNode[0];
    /**
     * java.util.PriorityQueue 默认大小
     **/
    private static final int DEFAULT_SIZE = 11;

    /**
     * 元素排序器
     **/
    private final Comparator<T> comparator;
    /**
     * 容器
     **/
    private T[] queue;
    /**
     * 元素数量
     **/
    private int size;

    public DefaultDynamicsPriorityQueue(Comparator<T> comparator) {
        this(comparator, DEFAULT_SIZE);
    }

    @SuppressWarnings("unchecked")
    public DefaultDynamicsPriorityQueue(Comparator<T> comparator, int initialSize) {
        ObjectUtils.checkNotNull(comparator, "comparator");
        this.comparator = comparator;
        this.queue = (T[]) (initialSize > 0 ? new PriorityQueueNode[initialSize] : EMPTY_ARRAY);
    }


    @Override
    public void priorityChanged(T node) {
        int i = node.priorityQueueIndex(this);
        if (!contains(node, i)) {
            return;
        }

        if (i == 0) {
            bubbleDown(i, node);
        } else {
            int iParent = (i - 1) >>> 1;
            T parent = queue[iParent];
            if (comparator.compare(node, parent) < 0) {
                bubbleUp(i, node);
            } else {
                bubbleDown(i, node);
            }
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new PriorityQueueIterator();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean offer(T t) {
        int index = t.priorityQueueIndex(this);
        if (index != PriorityQueueNode.INDEX_NOT_IN_QUEUE) {
            ;
            throw new IllegalArgumentException("e.priorityQueueIndex(): " + index + " (expected: "
                    + PriorityQueueNode.INDEX_NOT_IN_QUEUE + ") + e: " + t);
        }

        // 检查容量
        if (size >= queue.length) {
            // 扩容
            queue = Arrays.copyOf(queue, queue.length
                    + ((queue.length < 64) ? queue.length + 2 : (queue.length >>> 1)));
        }

        bubbleUp(size++, t);
        return true;
    }

    @Override
    public T poll() {
        if (size == 0) {
            return null;
        }
        T result = queue[0];
        result.priorityQueueIndex(this, PriorityQueueNode.INDEX_NOT_IN_QUEUE);

        T last = queue[--size];
        queue[size] = null;
        if (size != 0) {
            bubbleDown(0, last);
        }

        return result;
    }

    @Override
    public T peek() {
        return size == 0 ? null : queue[0];
    }

    @Override
    public boolean contains(Object o) {
        return super.contains(o);
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(queue, size);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X> X[] toArray(X[] a) {
        if (a.length < size) {
            return (X[]) Arrays.copyOf(queue, size, a.getClass());
        }
        System.arraycopy(queue, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public boolean remove(Object o) {
        final T node;
        try {
            node = (T) o;
        } catch (ClassCastException e) {
            return false;
        }
        return remove0(node);
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; ++i) {
            T node = queue[i];
            if (node != null) {
                node.priorityQueueIndex(this, PriorityQueueNode.INDEX_NOT_IN_QUEUE);
                queue[i] = null;
            }
        }

        size = 0;
    }

    /**
     * 从指定位置开始上浮(最小堆)
     *
     * @param k    索引
     * @param node 元素
     */
    private void bubbleUp(int k, T node) {
        while (k > 0) {
            int iParent = (k - 1) >>> 1;
            T parent = queue[iParent];

            // 最小堆性质
            // 如果元素小于上层元素,即意味着找到了该位置
            if (comparator.compare(node, parent) >= 0) {
                break;
            }

            // 否则让上层元素下沉
            queue[k] = parent;
            parent.priorityQueueIndex(this, k);

            // 开始下一次循环
            k = iParent;
        }

        // 找到元素的位置,并能维持住最小堆的性质
        queue[k] = node;
        node.priorityQueueIndex(this, k);
    }

    /**
     * 从指定位置开始下沉(最小堆性质)
     *
     * @param k    索引
     * @param node 元素
     */
    private void bubbleDown(int k, T node) {
        final int half = size >>> 1;
        while (k < half) {
            // k的下层起始索引
            int iChild = (k << 1) + 1;
            T child = queue[iChild];

            // 确保得到最小的下层元素
            int rightChild = iChild + 1;
            if (rightChild < size && comparator.compare(child, queue[rightChild]) > 0) {
                child = queue[iChild = rightChild];
            }

            // 小于等于下层最小元素则停止
            if (comparator.compare(node, child) <= 0) {
                break;
            }
            // 下沉元素
            queue[k] = child;
            child.priorityQueueIndex(this, k);

            // 开始下次循环
            k = iChild;
        }

        // 得到元素的位置
        queue[k] = node;
        node.priorityQueueIndex(this, k);
    }

    private boolean remove0(T node) {
        int i = node.priorityQueueIndex(this);
        if (!contains(node, i)) {
            return false;
        }

        node.priorityQueueIndex(this, PriorityQueueNode.INDEX_NOT_IN_QUEUE);
        if (--size == 0 || size == i) {
            queue[i] = null;
            return true;
        }

        // 将最大的元素移到删除的元素位置上
        T moved = queue[i] = queue[size];
        queue[size] = null;
        // 确保保持最小堆性质
        if (comparator.compare(node, moved) < 0) {
            bubbleDown(i, moved);
        } else {
            bubbleUp(i, moved);
        }

        return true;

    }

    private boolean contains(PriorityQueueNode node, int i) {
        return i >= 0 && i < size && node.equals(queue[i]);
    }

    private final class PriorityQueueIterator implements Iterator<T> {
        private int index;

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public T next() {
            if (index >= size) {
                throw new NoSuchElementException();
            }
            return queue[index++];
        }

        @Override
        public void remove() {
            int lastIndex = index - 1;
            if (lastIndex < 0 || lastIndex >= size) {
                throw new NoSuchElementException();
            }

            T removed = queue[lastIndex];
            remove0(removed);
            index = lastIndex;
        }
    }

}
