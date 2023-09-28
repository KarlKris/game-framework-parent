package com.echo.common.id;

/**
 * 分布式id之雪花算法
 * 64位具体分布
 * 1位用作最高位保留
 * 41位作为毫秒数 41位的长度可以使用69年
 * 2位作为时间回拨位 同一毫秒支持4次回拨
 * 10位作为机器位 10位的长度最多支持部署1024个节点
 * 10位作为毫秒内序号位  10位的计数顺序号支持每个节点每毫秒产生1024个ID序号
 *
 * @author li-yuanwen
 **/
public class SnowflakeIdGenerator implements DistributedIdGenerator {


    //================================================Algorithm's Parameter=============================================

    /**
     * 系统开始时间截 (UTC 2020-01-01 00:00:00)
     **/
    private final static long START_TIME = 1577808000000L;
    /**
     * 时间回拨位
     **/
    private final static int TIME_BACK_BITS = 2;
    /**
     * 机器id所占的位数
     **/
    private final static int WORKER_ID_BITS = 10;
    /**
     * 支持的最大机器id(十进制)，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     * -1L 左移 5位 (worker id 所占位数) 即 5位二进制所能获得的最大十进制数 - 31
     **/
    private final static int MAX_WORKER_ID = ~(-1 << WORKER_ID_BITS);
    /**
     * 序列在id中占的位数
     **/
    private final static int SEQUENCE_BITS = 10;
    /**
     * 机器ID 左移位数 - 10 (即末 sequence 所占用的位数)
     **/
    private final static int WORKER_ID_MOVE_BITS = SEQUENCE_BITS;
    /**
     * 时间截向 左移位数 - 22(10+10+2)
     **/
    private final static int TIMESTAMP_MOVE_BITS = SEQUENCE_BITS + WORKER_ID_BITS + TIME_BACK_BITS;
    /**
     * 时间回拨ID 左移位数 - 20 (10+10)
     **/
    private final static int TIME_BACK_ID_MOVE_BITS = SEQUENCE_BITS + WORKER_ID_BITS;
    /**
     * 生成序列的掩码(10位所对应的最大整数值)，这里为1023
     **/
    private final static int SEQUENCE_MARK = ~(-1 << SEQUENCE_BITS);
    /**
     * 时间回调的掩码(2位所对应的最大整数值)，这里为3
     **/
    private final static int TIME_BACK_MARK = ~(-1 << TIME_BACK_BITS);
    /**
     * 机器ID掩码
     **/
    private final static int WORKER_ID_MARK = ~(-1 << WORKER_ID_BITS) << WORKER_ID_MOVE_BITS;


    //=================================================Works's Parameter================================================

    /**
     * 上次生成ID的时间截
     **/
    private long lastTimestamp = -1L;
    /**
     * 时间回拨
     **/
    private long timeBackId = 0L;
    /**
     * 工作机器ID(0~31)
     **/
    private final long workerId;
    /**
     * 毫秒内序列(0~4095)
     **/
    private long sequence = 0L;

    /**
     * 根据id查询机器id
     **/
    public static int toWorkerId(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("Id can't be less than 0");
        }

        return (short) ((id & WORKER_ID_MARK) >> WORKER_ID_MOVE_BITS);
    }

    //===============================================Constructors=======================================================

    /**
     * 构造函数
     *
     * @param workerId 机器ID (0~1023)
     */
    public SnowflakeIdGenerator(short workerId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("Worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        this.workerId = workerId;
    }

    // ==================================================Methods========================================================


    @Override
    public int getWorkerId(long id) {
        return toWorkerId(id);
    }

    /**
     * 线程安全的获得下一个 ID 的方法
     **/
    @Override
    public synchronized long nextId() {
        long timestamp = currentTime();
        //如果当前时间小于上一次ID生成的时间戳: 说明系统时钟回退过 - 这个时候时间回拨位加1
        if (timestamp < lastTimestamp) {
            timeBackId = (timeBackId + 1) & TIME_BACK_MARK;
        }
        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MARK;
            //毫秒内序列溢出 即 序列 > 4095
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = blockTillNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }
        //上次生成ID的时间截
        lastTimestamp = timestamp;
        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - START_TIME) << TIMESTAMP_MOVE_BITS)
                | (timeBackId << TIME_BACK_ID_MOVE_BITS)
                | (workerId << WORKER_ID_MOVE_BITS)
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒 即 直到获得新的时间戳
     **/
    protected long blockTillNextMillis(long lastTimestamp) {
        long timestamp = currentTime();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTime();
        }
        return timestamp;
    }

    /**
     * 获得以毫秒为单位的当前时间
     **/
    protected long currentTime() {
        return System.currentTimeMillis();
    }

}  