package com.echo.redis.core;

import io.lettuce.core.ScoredValue;
import io.lettuce.core.api.StatefulConnection;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis 常用命令集
 */
public interface RedisOperations {

    /**
     * key 序列化/反序列化
     *
     * @return
     */
    RedisSerializer getKeySerializer();

    /**
     * value 序列化/反序列化
     *
     * @return
     */
    RedisSerializer getValueSerializer();

    /**
     * lettuce connection
     *
     * @return
     */
    StatefulConnection<byte[], byte[]> getConnection();

    // ----------------- key常用命令 -----------------------------------

    /**
     * 根据正则表达式获取对象
     *
     * @param pattern 正则表达式
     * @return
     */
    List<String> keys(final String pattern);

    /**
     * 根据key判断某一对象是否存在
     *
     * @param key
     * @return 是否存在
     */
    Boolean exists(final String key);

    /**
     * 根据key删除对象
     *
     * @param key
     */
    long del(final String key);

    /**
     * 根据正则表达式删除对象
     *
     * @param pattern 正则表达式
     * @return
     */
    long delAll(final String pattern);

    /**
     * 根据key获取对应对象的类型
     *
     * @param key
     * @return 对应对象的类型
     */
    String type(final String key);

    /**
     * 设置key的过期时间
     *
     * @param key
     * @param timeout
     * @return 是否设置成功
     */
    Boolean expire(final String key, final int timeout, final TimeUnit unit);

    /**
     * 设置key在指定时间点后过期
     *
     * @param key
     * @param unixTime
     * @return 是否成功
     */
    Boolean expireAt(final String key, final long unixTime);

    /**
     * 获取对应key的过期时间
     *
     * @param key
     * @return
     */
    Long ttl(final String key);


    // ----------------- string数据结构常用命令 --------------------------

    /**
     * 设置key-value
     *
     * @param key
     * @param value
     * @param seconds 过期时间(秒)
     */
    boolean setEx(final String key, final Object value, long seconds);

    /**
     * 设置key-value 过期时间使用默认配置值
     *
     * @param key
     * @param value
     */
    boolean set(final String key, final Object value);

    /**
     * 根据key获取对象
     *
     * @param key
     * @return
     */
    <T> T get(final String key, final Class<T> tClass);

    /**
     * 设置新值并返回旧值
     *
     * @param key
     * @param value
     * @return 旧值
     */
    <T> T getAndSet(final String key, final T value);

    /**
     * 指定的 key 不存在时,为 key 设置指定的value
     *
     * @param key
     * @param value
     * @return 是否设置成功
     */
    boolean setNx(final String key, final Object value);

    /**
     * 对应key的值自增
     *
     * @param key
     * @return 自增后的值
     */
    Long incr(final String key);

    /**
     * 对应key的数值增加increment
     *
     * @param key
     * @param increment
     * @return
     */
    Long incrBy(final String key, final long increment);

    /**
     * 对应key的数值自减
     *
     * @param key
     * @return
     */
    Long decr(final String key);

    /**
     * 对应key的数值减去decrement
     *
     * @param key
     * @param decrement
     * @return
     */
    Long decrBy(final String key, final long decrement);

    // ----------------- hash数据结构常用命令 ------------------------------

    /**
     * 根据key设置对应哈希表对象的field - value
     *
     * @param key
     * @param field
     * @param value
     */
    boolean hSet(final String key, final String field, final Object value);

    /**
     * 根据key获取对应哈希表的对应field的对象
     *
     * @param key
     * @param field
     * @return
     */
    <T> T hGet(final String key, final String field, final Class<T> tClass);

    /**
     * 根据key删除对应哈希表的对应field的对象
     *
     * @param key
     * @param field
     * @return
     */
    long hDel(final String key, final String field);

    /**
     * 获取在哈希表中指定 key 的所有字段和值
     * redis command: HGETALL key
     *
     * @param key
     * @return
     */
    Map<String, byte[]> hEntries(final String key);

    /**
     * 获取所有给定字段的值
     *
     * @param key
     * @param fields
     * @return
     */
    Map<String, byte[]> hMultiGet(final String key, final String... fields);

    /**
     * 为哈希表 key 中的指定字段的整数值加上增量 increment
     * redis command: HINCRBY key field increment
     *
     * @param key
     * @param field
     * @param increment
     * @return
     */
    Long hIncrBy(final String key, final String field, final long increment);


    /**
     * 同时将多个 field-value (域-值)对设置到哈希表 key 中。
     * redis command: HMSET key field1 value1 [field2 value2 ]
     *
     * @param key
     * @param content
     */
    boolean hMultiSet(final String key, final Map<String, Object> content);


    /**
     * 只有在字段 field 不存在时，设置哈希表字段的值。
     * redis command: HSETNX key field value
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    boolean hSetNx(final String key, final String field, final Object value);

    // ----------------- list数据结构常用命令 ------------------------------

    /**
     * 移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     * redis command: LPOP key1 [count ]
     *
     * @param key
     * @param tClass
     * @param <T>
     * @return
     */
    <T> T lLeftPop(final String key, final Class<T> tClass);

    /**
     * 移出并获取列表的 count 元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     * redis command: LPOP key1 [count ]
     *
     * @param key
     * @param tClass
     * @param <T>
     * @return
     */
    <T> List<T> lLeftPop(final String key, final long count, final Class<T> tClass);


    /**
     * 移出并获取列表的 count 元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     * redis command: RPOP key1 [key2 ]
     *
     * @param key
     * @param tClass
     * @param <T>
     * @return
     */
    <T> List<T> lRightPop(final String key, final long count, final Class<T> tClass);

    /**
     * 移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     * redis command: RPOP key1 [key2 ]
     *
     * @param key
     * @param tClass
     * @param <T>
     * @return
     */
    <T> T lRightPop(final String key, final Class<T> tClass);


    /**
     * 从列表中弹出一个值，将弹出的元素插入到另外一个列表中并返回它；
     * 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     * redis command: RPOPLPUSH source destination
     *
     * @param sourceKey
     * @param targetKey
     * @param tClass
     * @param <T>
     * @return
     */
    <T> T lRightPopLeftPush(final String sourceKey, final String targetKey, final Class<T> tClass);

    /**
     * 通过索引获取列表中的元素
     * redis command: LINDEX key index
     *
     * @param key
     * @param index
     * @param <T>
     * @return
     */
    <T> T lIndex(final String key, final long index, final Class<T> tClass);

    /**
     * 通过索引设置列表元素的值
     * redis command: LSET key index value
     *
     * @param key
     * @param index
     * @param value
     */
    boolean lSet(final String key, final long index, final Object value);


    /**
     * 获取列表指定范围内的元素
     * redis command:  LRANGE key start stop
     *
     * @param key
     * @param start
     * @param end
     * @param <T>
     * @return
     */
    <T> List<T> lRange(final String key, final Class<T> tClass, final long start, final long end);


    /**
     * 将一个或多个插入到列表头部
     * redis command: LPUSH key value1 [value2]
     *
     * @param key
     * @param value
     * @return
     */
    long lLeftPush(final String key, final Object... value);


    /**
     * 将一个值插入到已存在的列表头部
     * redis command: LPUSHX key value
     *
     * @param key
     * @param value
     * @return
     */
    long lLeftPushNx(final String key, final Object... value);


    /**
     * 在列表中添加一个或多个值
     * redis command: RPUSH key value1 [value2]
     *
     * @param key
     * @param value
     * @return
     */
    long lRightPush(final String key, final Object... value);

    /**
     * 为已存在的列表添加值
     * redis command: RPUSHX key value
     *
     * @param key
     * @param value
     * @return
     */
    long lRightPushNx(final String key, final Object... value);


    /**
     * 获取列表长度
     * redis command: LLEN key
     *
     * @param key
     * @return
     */
    long lSize(final String key);


    /**
     * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
     * redis command: LTRIM key start stop
     *
     * @param key
     * @param start
     * @param end
     */
    boolean lTrim(final String key, final long start, final long end);

    // ----------------- set数据结构常用命令 -------------------------------

    /**
     * 将value设置至指定key的set集合中
     * redis command: SADD key member1 [member2]
     *
     * @param key
     * @param value
     */
    long sAdd(final String key, final Object... value);

    /**
     * 获取集合的成员数
     * redis command: SCARD key
     *
     * @param key
     * @return
     */
    long sSize(final String key);

    /**
     * 获取指定key的set集合
     * redis command: SMEMBERS key
     *
     * @param key
     * @return
     */
    <T> Set<T> sAll(final String key, final Class<T> tClass);

    /**
     * 删除指定key的set集合中的value
     * redis command: SREM key member1 [member2]
     *
     * @param key
     * @param value
     * @return
     */
    long sDel(final String key, final Object... value);

    /**
     * 判断 member 元素是否是集合 key 的成员
     * redis command: SISMEMBER key member
     *
     * @param key
     * @param member
     * @return
     */
    boolean sIsMember(final String key, final Object member);


    /**
     * 将 member 元素从 source 集合移动到 destination 集合
     * redis command: SMOVE source destination member
     *
     * @param key
     * @param value
     * @param destinationKey
     * @return
     */
    boolean sMove(final String key, final Object value, final String destinationKey);

    /**
     * 返回给定所有集合的交集
     * redis command: SINTER key1 [key2]
     *
     * @param tClass
     * @param keys
     * @param <T>
     * @return
     */
    <T> Set<T> sIntersect(final Class<T> tClass, final String... keys);

    /**
     * 返回给定所有集合的交集并存储在 destinationKey 中
     *
     * @param destinationKey
     * @param keys
     * @return
     */
    long sIntersectAndStore(final String destinationKey, final String... keys);

    /**
     * 返回给定所有集合的差集
     * redis command: SDIFF key1 [key2]
     *
     * @param tClass
     * @param keys
     * @param <T>
     * @return
     */
    <T> Set<T> sDiff(final Class<T> tClass, final String... keys);


    /**
     * 给定所有集合的差集并存储在 destinationKey 中
     * redis command: SDIFFSTORE destination key1 [key2]
     *
     * @param destinationKey
     * @param keys
     * @return 差集成员数
     */
    long sDiffAndStore(final String destinationKey, final String... keys);


    /**
     * 返回所有给定集合的并集
     * redis command: SUNION key1 [key2]
     *
     * @param tClass
     * @param keys
     * @param <T>
     * @return
     */
    <T> Set<T> sUnion(final Class<T> tClass, final String... keys);


    /**
     * 所有给定集合的并集存储在 destinationKey 集合中
     * redis command: SUNIONSTORE destination key1 [key2]
     *
     * @param destinationKey
     * @param keys
     * @return
     */
    long sUnionAndStore(final String destinationKey, final String... keys);

    // ----------------- sorted set数据结构常用命令 ------------------------


    /**
     * 向有序集合添加一个成员，或者更新已存在成员的分数
     * reids command: ZADD key score1 member1 [score2 member2]
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    long zAdd(final String key, final Object value, final double score);


    /**
     * 向有序集合添加多个成员，或者更新已存在成员的分数
     * reids command: ZADD key score1 member1 [score2 member2]
     *
     * @param key
     * @param member
     * @return
     */
    Long zAdd(final String key, final ScoredValue<?>... member);


    /**
     * 移除有序集合中的一个或多个成员
     * redis command:  ZREM key member [member ...]
     *
     * @param key
     * @param member
     * @return
     */
    long zDel(final String key, final Object... member);

    /**
     * 获取有序集合的成员数
     * redis command: ZCARD key
     *
     * @param key
     * @return
     */
    long zSize(final String key);


    /**
     * 有序集合中对指定成员的分数加上增量 increment
     * redis command: ZINCRBY key increment member
     *
     * @param key
     * @param member
     * @param increment
     * @return
     */
    double zIncrementScore(final String key, final Object member, final double increment);

    /**
     * 返回有序集合中指定成员的索引
     * redis command: ZRANK key member
     *
     * @param key
     * @param member
     * @return
     */
    long zRank(final String key, final Object member);

    /**
     * 返回有序集合中指定成员的排名，有序集成员按分数值递减(从大到小)排序
     * redis command: ZREVRANK key member
     *
     * @param key
     * @param member
     * @return
     */
    long zReverseRank(final String key, final Object member);

    /**
     * 返回有序集中，成员的分数值
     * redis command: ZSCORE key member
     *
     * @param key
     * @param member
     * @return
     */
    double zScore(final String key, final Object member);

    /**
     * 通过索引区间返回有序集合成指定区间内的成员
     * redis command: ZRANGE key start stop [WITHSCORES]
     *
     * @param key
     * @param start
     * @param end
     * @param <T>
     * @return
     */
    <T> List<T> zRange(final String key, final Class<T> tClass, final long start, final long end);

    /**
     * 通过索引区间返回有序集合成指定区间内的成员
     * redis command: ZRANGE key start stop [WITHSCORES]
     *
     * @param key
     * @param tClass
     * @param start
     * @param end
     * @param <T>
     * @return
     */
    <T> List<ScoredValue<T>> zRangeWithScore(final String key, final Class<T> tClass, final long start, final long end);

    /**
     * 通过分数返回有序集合指定区间内的成员
     * redis command: ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT]
     *
     * @param key
     * @param tClass
     * @param min
     * @param max
     * @param <T>
     * @return
     */
    <T> List<T> zRangeByScore(final String key, final Class<T> tClass, final double min, final double max);

    /**
     * 通过分数返回有序集合指定区间内的成员
     * redis command: ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT]
     *
     * @param key
     * @param tClass
     * @param min
     * @param max
     * @param <T>
     * @return
     */
    <T> List<ScoredValue<T>> zRangeWithScoreByScore(final String key, final Class<T> tClass, final double min, final double max);

    /**
     * 计算在有序集合中指定区间分数的成员数
     * redis command: ZCOUNT key min max
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    long zCount(final String key, final double min, final double max);

    /**
     * 移除有序集合中给定的排名区间的所有成员
     * redis command: ZREMRANGEBYRANK key start stop
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    long zDelRange(final String key, final long start, final long end);

    /**
     * 移除有序集合中给定的分数区间的所有成员
     * redis command: ZREMRANGEBYSCORE key min max
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    long zDelRangeByScore(final String key, final double min, final double max);

    /**
     * 返回给定所有集合的交集
     * redis command: ZINTER key1 [key2]
     *
     * @param tClass
     * @param keys
     * @param <T>
     * @return
     */
    <T> List<T> zIntersect(final Class<T> tClass, final String... keys);

    /**
     * 计算给定的一个或多个有序集的交集并将结果集存储在新的有序集合 destinationKey 中
     * redis command：ZINTERSTORE destination numkeys key [key ...]
     *
     * @param destinationKey
     * @param keys
     * @return
     */
    long zInterAndStore(final String destinationKey, final String... keys);


    /**
     * 返回所有给定集合的并集
     * redis command: ZUNION key1 [key2]
     *
     * @param tClass
     * @param keys
     * @param <T>
     * @return
     */
    <T> List<T> zUnion(final Class<T> tClass, final String... keys);


    /**
     * 计算给定的一个或多个有序集的并集并将结果集存储在新的有序集合 destinationKey 中
     * redis command： ZUNIONSTORE destination numkeys key [key ...]
     *
     * @param destinationKey
     * @param keys
     * @return
     */
    long zUnionAndStore(final String destinationKey, final String... keys);

}
