package com.echo.common.id;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于雪花算法改良版,采用秒级别,支持过万的机器数
 * 使用标记位+节点id+时间戳+序列号的方式组成,不是全局单调递增，只是分机器单调递增
 * 64位具体分布
 * 1位用作最高位保留
 * 31位作为秒数 31位的长度可以使用68年
 * 16位作为机器位 16位的长度最多支持部署65536个节点
 * 16位作为秒内序号位  16位的计数顺序号支持每个节点每秒产生个65536ID序号
 *
 * @author: li-yuanwen
 */
public class MultiServerIdGenerator implements DistributedIdGenerator {

    //================================================Algorithm's Parameter=============================================

    /**
     * 系统开始时间截 (UTC 2020-01-01 00:00:00) 支持到 2088-01-19 03:14:07
     **/
    private final static int START_TIME = 1577808000;
    /**
     * 机器id所占的位数
     **/
    private final static int WORKER_ID_BITS = 16;
    /**
     * 支持的最大机器id(十进制)，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     * -1L 左移 5位 (worker id 所占位数) 即 5位二进制所能获得的最大十进制数 - 31
     **/
    private final static long MAX_WORKER_ID = ~(-1 << WORKER_ID_BITS);
    /**
     * 序列在id中占的位数 -> 16
     **/
    private final static int SEQUENCE_BITS = 16;
    /**
     * 机器ID 左移位数 -> 16+31 (即末 sequence 所占用的位数)
     **/
    private final static int WORKER_ID_MOVE_BITS = SEQUENCE_BITS + 31;
    /**
     * 时间戳 左移位数 - 16
     **/
    private final static int TIMESTAMP_MOVE_BITS = SEQUENCE_BITS;
    /**
     * 机器ID掩码
     **/
    private final static long WORKER_ID_MARK = MAX_WORKER_ID << WORKER_ID_MOVE_BITS;


    //=================================================Works's Parameter================================================

    /**
     * 工作机器ID(0~31)
     **/
    private final long workerId;

    private final AtomicLong idSequence;

    /**
     * 根据id查询机器id
     **/
    public static int toWorkerId(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("Id can't be less than 0");
        }

        return (int) ((id & WORKER_ID_MARK) >> WORKER_ID_MOVE_BITS);
    }

    //===============================================Constructors=======================================================

    /**
     * 构造函数
     *
     * @param workerId 机器ID (0~65535)
     */
    public MultiServerIdGenerator(int workerId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("Worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        this.workerId = workerId;
        long start = (this.workerId << WORKER_ID_MOVE_BITS) | ((currentTime() - START_TIME) << TIMESTAMP_MOVE_BITS);
        this.idSequence = new AtomicLong(start);
    }

    // ==================================================Methods========================================================


    public long getWorkerId() {
        return workerId;
    }

    @Override
    public int getWorkerId(long id) {
        return toWorkerId(id);
    }

    /**
     * 线程安全的获得下一个 ID 的方法
     **/
    @Override
    public long nextId() {
        return idSequence.incrementAndGet();
    }

    /**
     * 获得以毫秒为单位的当前时间
     **/
    protected long currentTime() {
        return Instant.now().getEpochSecond();
    }


}
