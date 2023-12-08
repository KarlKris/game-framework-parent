package com.echo.redis.core;

import com.echo.redis.lettuce.LettuceConnectionFactory;
import io.lettuce.core.KeyValue;
import io.lettuce.core.Range;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * redis 常见命令模板
 * key, hashKey 均使用 {@link String}
 *
 * @author: li-yuanwen
 */
public class RedisTemplate implements RedisOperations, RedisPubSubOperations {


    private final RedisSerializer keySerializer;
    private RedisSerializer valueSerializer;

    private LettuceConnectionFactory lettuceConnectionFactory;

    public RedisTemplate(RedisSerializer valueSerializer, LettuceConnectionFactory lettuceConnectionFactory) {
        this(StringRedisSerializer.UTF_8, valueSerializer, lettuceConnectionFactory);
    }

    public RedisTemplate(RedisSerializer keySerializer, RedisSerializer valueSerializer, LettuceConnectionFactory lettuceConnectionFactory) {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.lettuceConnectionFactory = lettuceConnectionFactory;
    }

    public LettuceConnectionFactory getRedisConnectionFactory() {
        return lettuceConnectionFactory;
    }

    public void setRedisConnectionFactory(LettuceConnectionFactory lettuceConnectionFactory) {
        this.lettuceConnectionFactory = lettuceConnectionFactory;
    }

    @Override
    public RedisSerializer getKeySerializer() {
        return keySerializer;
    }

    @Override
    public RedisSerializer getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(RedisSerializer valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    @Override
    public StatefulConnection<byte[], byte[]> getConnection() {
        return lettuceConnectionFactory.getConnection();
    }


    public RedisClusterCommands<byte[], byte[]> commands() {
        return lettuceConnectionFactory.getCommands();
    }

    public RedisClusterAsyncCommands<byte[], byte[]> asyncCommands() {
        return lettuceConnectionFactory.getAsyncCommands();
    }

    public <T> T execute(RedisCallback<T> callback) {
        return callback.doInRedis(commands());
    }

    private String keyDeserialize(byte[] bytes) {
        return keySerializer.deserialize(bytes, String.class);
    }

    private byte[][] rawKeys(String... keys) {
        byte[][] array = new byte[keys.length][];
        for (int i = 0; i < keys.length; i++) {
            array[i] = keySerializer.serialize(keys[i]);
        }
        return array;
    }

    private byte[][] rawKeys(Collection<String> keys) {
        byte[][] array = new byte[keys.size()][];
        int i = 0;
        for (String key : keys) {
            array[i++] = keySerializer.serialize(key);
        }
        return array;
    }

    private byte[][] rawValues(Object... elements) {
        byte[][] array = new byte[elements.length][];
        for (int i = 0; i < elements.length; i++) {
            array[i] = valueSerializer.serialize(elements[i]);
        }
        return array;
    }

    private byte[][] rawValues(Collection<Object> elements) {
        byte[][] array = new byte[elements.size()][];
        int i = 0;
        for (Object value : elements) {
            array[i++] = valueSerializer.serialize(value);
        }
        return array;
    }

    private <T> List<T> handleResponseByteArrayList(List<byte[]> response, Class<T> tClass) {
        if (response == null || response.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<T> list = new ArrayList<>(response.size());
        for (byte[] bytes : response) {
            list.add(valueSerializer.deserialize(bytes, tClass));
        }
        return list;
    }

    private <T> List<ScoredValue<T>> handleResponseScoreValueList(List<ScoredValue<byte[]>> response, Class<T> tClass) {
        if (response == null || response.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<ScoredValue<T>> list = new ArrayList<>(response.size());
        for (ScoredValue<byte[]> value : response) {
            list.add(ScoredValue.just(value.getScore(), valueSerializer.deserialize(value.getValue(), tClass)));
        }
        return list;
    }

    private <T> Set<T> handleResponseByteArraySet(Set<byte[]> response, Class<T> tClass) {
        if (response == null || response.isEmpty()) {
            return new HashSet<>(0);
        }
        Set<T> set = new HashSet<>(response.size());
        for (byte[] bytes : response) {
            set.add(valueSerializer.deserialize(bytes, tClass));
        }
        return set;
    }

    // --------------------------------------------- key常用命令 --------------------------------------------------------


    @Override
    public List<String> keys(String pattern) {
        final byte[] rawPattern = keySerializer.serialize(pattern);
        return execute(new RedisCallback<List<String>>() {
            @Override
            public List<String> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                List<byte[]> response = commands.keys(rawPattern);
                return response.stream().map(bytes -> keyDeserialize(bytes)).collect(Collectors.toList());
            }
        });
    }

    @Override
    public Boolean exists(String key) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.exists(rawKey) > 0;
            }
        });
    }

    @Override
    public long del(String key) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.del(rawKey);
            }
        });
    }

    @Override
    public long delAll(String pattern) {
        List<String> keys = keys(pattern);
        byte[][] rawPatterns = rawKeys(keys);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.del(rawPatterns);
            }
        });
    }

    @Override
    public String type(String key) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.type(rawKey);
            }
        });
    }

    @Override
    public Boolean expire(String key, int timeout, TimeUnit unit) {
        final long seconds = unit.toSeconds(timeout);
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.expire(rawKey, seconds);
            }
        });
    }

    @Override
    public Boolean expireAt(String key, long unixTime) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.expireat(rawKey, unixTime);
            }
        });
    }

    @Override
    public Long ttl(String key) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.ttl(rawKey);
            }
        });
    }

    // ------------------------------------ string数据结构常用命令 --------------------------------------------------------

    @Override
    public boolean setEx(String key, Object value, long seconds) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawValue = valueSerializer.serialize(value);
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return Converters.stringToBoolean(commands.setex(rawKey, seconds, rawValue));
            }
        });

    }

    @Override
    public boolean set(String key, Object value) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawValue = valueSerializer.serialize(value);
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return Converters.stringToBoolean(commands.set(rawKey, rawValue));
            }
        });
    }

    @Override
    public <T> T get(String key, Class<T> tClass) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<T>() {
            @Override
            public T doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return valueSerializer.deserialize(commands.get(rawKey), tClass);
            }
        });
    }

    @Override
    public <T> T getAndSet(String key, T value) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawValue = valueSerializer.serialize(value);
        final Class<T> tClass = (Class<T>) value.getClass();
        return execute(new RedisCallback<T>() {
            @Override
            public T doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return valueSerializer.deserialize(commands.getset(rawKey, rawValue), tClass);
            }
        });
    }

    @Override
    public boolean setNx(String key, Object value) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawValue = valueSerializer.serialize(value);
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.setnx(rawKey, rawValue);
            }
        });
    }

    @Override
    public Long incr(String key) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.incr(rawKey);
            }
        });
    }

    @Override
    public Long incrBy(String key, long increment) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.incrby(rawKey, increment);
            }
        });
    }

    @Override
    public Long decr(String key) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.decr(rawKey);
            }
        });
    }

    @Override
    public Long decrBy(String key, long decrement) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.decrby(rawKey, decrement);
            }
        });
    }

    // ------------------------------------- hash数据结构常用命令 ---------------------------------------------------------


    @Override
    public boolean hSet(String key, String field, Object value) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawField = keySerializer.serialize(field);
        final byte[] rawValue = valueSerializer.serialize(value);
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.hset(rawKey, rawField, rawValue);
            }
        });

    }

    @Override
    public <T> T hGet(String key, String field, Class<T> tClass) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawField = keySerializer.serialize(field);
        return execute(new RedisCallback<T>() {
            @Override
            public T doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return valueSerializer.deserialize(commands.hget(rawKey, rawField), tClass);
            }
        });
    }

    @Override
    public long hDel(String key, String field) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawField = keySerializer.serialize(field);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.hdel(rawKey, rawField);
            }
        });

    }

    @Override
    public Map<String, byte[]> hEntries(String key) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Map<String, byte[]>>() {
            @Override
            public Map<String, byte[]> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                Map<byte[], byte[]> response = commands.hgetall(rawKey);
                Map<String, byte[]> map = new HashMap<>(response.size());
                for (Map.Entry<byte[], byte[]> entry : response.entrySet()) {
                    map.put(keyDeserialize(entry.getKey()), entry.getValue());
                }
                return map;
            }
        });
    }

    @Override
    public Map<String, byte[]> hMultiGet(String key, String... fields) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[][] rawFields = rawKeys(fields);
        return execute(new RedisCallback<Map<String, byte[]>>() {
            @Override
            public Map<String, byte[]> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                List<KeyValue<byte[], byte[]>> list = commands.hmget(rawKey, rawFields);
                Map<String, byte[]> map = new HashMap<>(list.size());
                for (KeyValue<byte[], byte[]> entry : list) {
                    map.put(keyDeserialize(entry.getKey()), entry.getValue());
                }
                return map;
            }
        });
    }

    @Override
    public Long hIncrBy(String key, String field, long increment) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawField = keySerializer.serialize(field);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.hincrby(rawKey, rawField, increment);
            }
        });
    }

    @Override
    public boolean hMultiSet(String key, Map<String, Object> content) {
        final byte[] rawKey = keySerializer.serialize(key);
        final Map<byte[], byte[]> map = new HashMap<>(content.size());
        for (Map.Entry<String, Object> entry : content.entrySet()) {
            map.put(keySerializer.serialize(entry.getKey()), valueSerializer.serialize(entry.getValue()));
        }
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return Converters.stringToBoolean(commands.hmset(rawKey, map));
            }
        });
    }

    @Override
    public boolean hSetNx(String key, String field, Object value) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawField = keySerializer.serialize(field);
        final byte[] rawValue = valueSerializer.serialize(value);
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.hsetnx(rawKey, rawField, rawValue);
            }
        });
    }

    // ----------------------------------- list数据结构常用命令 -----------------------------------------------------------


    @Override
    public <T> T lLeftPop(String key, Class<T> tClass) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<T>() {
            @Override
            public T doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return valueSerializer.deserialize(commands.lpop(rawKey), tClass);
            }
        });
    }

    @Override
    public <T> List<T> lLeftPop(String key, long count, Class<T> tClass) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<List<T>>() {
            @Override
            public List<T> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return handleResponseByteArrayList(commands.lpop(rawKey, count), tClass);
            }
        });
    }

    @Override
    public <T> T lRightPop(String key, Class<T> tClass) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<T>() {
            @Override
            public T doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return valueSerializer.deserialize(commands.rpop(rawKey), tClass);
            }
        });
    }

    @Override
    public <T> List<T> lRightPop(String key, long count, Class<T> tClass) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<List<T>>() {
            @Override
            public List<T> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return handleResponseByteArrayList(commands.rpop(rawKey, count), tClass);
            }
        });
    }

    @Override
    public <T> T lRightPopLeftPush(String sourceKey, String targetKey, Class<T> tClass) {
        final byte[] rawSourceKey = keySerializer.serialize(sourceKey);
        final byte[] rawTargetKey = keySerializer.serialize(targetKey);
        return execute(new RedisCallback<T>() {
            @Override
            public T doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return valueSerializer.deserialize(commands.rpoplpush(rawSourceKey, rawTargetKey), tClass);
            }
        });
    }


    @Override
    public <T> T lIndex(String key, long index, Class<T> tClass) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<T>() {
            @Override
            public T doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return valueSerializer.deserialize(commands.lindex(rawKey, index), tClass);
            }
        });
    }

    @Override
    public boolean lSet(String key, long index, Object value) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawValue = valueSerializer.serialize(value);
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return Converters.stringToBoolean(commands.lset(rawKey, index, rawValue));
            }
        });
    }

    @Override
    public <T> List<T> lRange(String key, final Class<T> tClass, long start, long end) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<List<T>>() {
            @Override
            public List<T> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return handleResponseByteArrayList(commands.lrange(rawKey, start, end), tClass);
            }
        });
    }

    @Override
    public long lLeftPush(String key, Object... value) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[][] rawValue = rawValues(value);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.lpush(rawKey, rawValue);
            }
        });
    }

    @Override
    public long lLeftPushNx(String key, Object... value) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[][] rawValue = rawValues(value);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.lpushx(rawKey, rawValue);
            }
        });
    }


    @Override
    public long lRightPush(String key, Object... value) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[][] rawValue = rawValues(value);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.rpush(rawKey, rawValue);
            }
        });
    }

    @Override
    public long lRightPushNx(String key, Object... value) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[][] rawValue = rawValues(value);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.rpushx(rawKey, rawValue);
            }
        });
    }

    @Override
    public long lSize(String key) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.llen(rawKey);
            }
        });
    }

    @Override
    public boolean lTrim(String key, long start, long end) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return Converters.stringToBoolean(commands.ltrim(rawKey, start, end));
            }
        });
    }

    // ------------------------------------------ set数据结构常用命令 -----------------------------------------------------


    @Override
    public long sAdd(String key, Object... value) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[][] rawValue = rawValues(value);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.sadd(rawKey, rawValue);
            }
        });
    }

    @Override
    public long sSize(String key) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.scard(rawKey);
            }
        });
    }

    @Override
    public <T> Set<T> sAll(String key, Class<T> tClass) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Set<T>>() {
            @Override
            public Set<T> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return handleResponseByteArraySet(commands.smembers(rawKey), tClass);
            }
        });
    }

    @Override
    public long sDel(String key, Object... value) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[][] rawValue = rawValues(value);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.srem(rawKey, rawValue);
            }
        });
    }

    @Override
    public boolean sIsMember(String key, Object member) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawValue = valueSerializer.serialize(member);
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.sismember(rawKey, rawValue);
            }
        });
    }

    @Override
    public boolean sMove(String key, Object value, String destinationKey) {
        final byte[] rawSourceKey = keySerializer.serialize(key);
        final byte[] rawTargetKey = keySerializer.serialize(destinationKey);
        final byte[] rawValue = valueSerializer.serialize(value);
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.smove(rawSourceKey, rawTargetKey, rawValue);
            }
        });
    }

    @Override
    public <T> Set<T> sIntersect(Class<T> tClass, String... keys) {
        final byte[][] rawKeys = rawKeys(keys);
        return execute(new RedisCallback<Set<T>>() {
            @Override
            public Set<T> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return handleResponseByteArraySet(commands.sinter(rawKeys), tClass);
            }
        });
    }

    @Override
    public long sIntersectAndStore(String destinationKey, String... keys) {
        final byte[] rawDestinationKey = keySerializer.serialize(destinationKey);
        final byte[][] rawKeys = rawKeys(keys);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.sinterstore(rawDestinationKey, rawKeys);
            }
        });
    }

    @Override
    public <T> Set<T> sDiff(Class<T> tClass, String... keys) {
        byte[][] rawKeys = rawKeys(keys);
        return execute(new RedisCallback<Set<T>>() {
            @Override
            public Set<T> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return handleResponseByteArraySet(commands.sdiff(rawKeys), tClass);
            }
        });
    }

    @Override
    public long sDiffAndStore(String destinationKey, String... keys) {
        final byte[] rawDestinationKey = keySerializer.serialize(destinationKey);
        final byte[][] rawKeys = rawKeys(keys);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.sdiffstore(rawDestinationKey, rawKeys);
            }
        });
    }

    @Override
    public <T> Set<T> sUnion(Class<T> tClass, String... keys) {
        final byte[][] rawKeys = rawKeys(keys);
        return execute(new RedisCallback<Set<T>>() {
            @Override
            public Set<T> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return handleResponseByteArraySet(commands.sunion(rawKeys), tClass);
            }
        });
    }

    @Override
    public long sUnionAndStore(String destinationKey, String... keys) {
        final byte[] rawDestinationKey = keySerializer.serialize(destinationKey);
        final byte[][] rawKeys = rawKeys(keys);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.sunionstore(rawDestinationKey, rawKeys);
            }
        });
    }

    // ---------------------------------- sorted set数据结构常用命令 ------------------------------------------------------


    @Override
    public long zAdd(String key, Object value, double score) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawValue = valueSerializer.serialize(value);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.zadd(rawKey, score, rawValue);
            }
        });
    }

    @Override
    public Long zAdd(String key, ScoredValue<?>... member) {
        final byte[] rawKey = keySerializer.serialize(key);
        final ScoredValue<byte[]>[] rawValues = new ScoredValue[member.length];
        for (int i = 0; i < member.length; i++) {
            rawValues[i] = ScoredValue.just(member[i].getScore(), valueSerializer.serialize(member[i].getValue()));
        }
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.zadd(rawKey, rawValues);
            }
        });
    }

    @Override
    public long zDel(String key, Object... member) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[][] rawValues = rawValues(member);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.zrem(rawKey, rawValues);
            }
        });
    }

    @Override
    public long zSize(String key) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.zcard(rawKey);
            }
        });
    }

    @Override
    public double zIncrementScore(String key, Object member, double increment) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawValue = valueSerializer.serialize(member);
        return execute(new RedisCallback<Double>() {
            @Override
            public Double doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.zincrby(rawKey, increment, rawValue);
            }
        });
    }

    @Override
    public long zRank(String key, Object member) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawValue = valueSerializer.serialize(member);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.zrank(rawKey, rawValue);
            }
        });
    }

    @Override
    public long zReverseRank(String key, Object member) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawValue = valueSerializer.serialize(member);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.zrevrank(rawKey, rawValue);
            }
        });
    }

    @Override
    public double zScore(String key, Object member) {
        final byte[] rawKey = keySerializer.serialize(key);
        final byte[] rawValue = valueSerializer.serialize(member);
        return execute(new RedisCallback<Double>() {
            @Override
            public Double doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.zscore(rawKey, rawValue);
            }
        });
    }

    @Override
    public <T> List<T> zRange(String key, Class<T> tClass, long start, long end) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<List<T>>() {
            @Override
            public List<T> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return handleResponseByteArrayList(commands.zrange(rawKey, start, end), tClass);
            }
        });
    }

    @Override
    public <T> List<ScoredValue<T>> zRangeWithScore(String key, Class<T> tClass, long start, long end) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<List<ScoredValue<T>>>() {
            @Override
            public List<ScoredValue<T>> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                List<ScoredValue<byte[]>> response = commands.zrangeWithScores(rawKey, start, end);
                return handleResponseScoreValueList(response, tClass);
            }
        });
    }

    @Override
    public <T> List<T> zRangeByScore(String key, Class<T> tClass, double min, double max) {
        final byte[] rawKey = keySerializer.serialize(key);
        final Range<Double> range = Range.create(min, max);
        return execute(new RedisCallback<List<T>>() {
            @Override
            public List<T> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return handleResponseByteArrayList(commands.zrangebyscore(rawKey, range), tClass);
            }
        });
    }

    @Override
    public <T> List<ScoredValue<T>> zRangeWithScoreByScore(String key, Class<T> tClass, double min, double max) {
        final byte[] rawKey = keySerializer.serialize(key);
        final Range<Double> range = Range.create(min, max);
        return execute(new RedisCallback<List<ScoredValue<T>>>() {
            @Override
            public List<ScoredValue<T>> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                List<ScoredValue<byte[]>> response = commands.zrangebyscoreWithScores(rawKey, range);
                return handleResponseScoreValueList(response, tClass);
            }
        });
    }

    @Override
    public long zCount(String key, double min, double max) {
        final byte[] rawKey = keySerializer.serialize(key);
        final Range<Double> range = Range.create(min, max);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.zcount(rawKey, range);
            }
        });
    }

    @Override
    public long zDelRange(String key, long start, long end) {
        final byte[] rawKey = keySerializer.serialize(key);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.zremrangebyrank(rawKey, start, end);
            }
        });
    }

    @Override
    public long zDelRangeByScore(String key, double min, double max) {
        final byte[] rawKey = keySerializer.serialize(key);
        final Range<Double> range = Range.create(min, max);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.zremrangebyscore(rawKey, range);
            }
        });
    }

    @Override
    public <T> List<T> zIntersect(Class<T> tClass, String... keys) {
        final byte[][] rawKeys = rawKeys(keys);
        return execute(new RedisCallback<List<T>>() {
            @Override
            public List<T> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return handleResponseByteArrayList(commands.zinter(rawKeys), tClass);
            }
        });
    }

    @Override
    public long zInterAndStore(String destinationKey, String... keys) {
        final byte[] rawDestinationKey = keySerializer.serialize(destinationKey);
        final byte[][] rawKeys = rawKeys(keys);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.zinterstore(rawDestinationKey, rawKeys);
            }
        });
    }

    @Override
    public <T> List<T> zUnion(Class<T> tClass, String... keys) {
        final byte[][] rawKeys = rawKeys(keys);
        return execute(new RedisCallback<List<T>>() {
            @Override
            public List<T> doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return handleResponseByteArrayList(commands.zunion(rawKeys), tClass);
            }
        });
    }

    @Override
    public long zUnionAndStore(String destinationKey, String... keys) {
        final byte[] rawDestinationKey = keySerializer.serialize(destinationKey);
        final byte[][] rawKeys = rawKeys(keys);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.zunionstore(rawDestinationKey, rawKeys);
            }
        });
    }

    @Override
    public long publish(String channel, Object msg) {
        final byte[] rawChannel = keySerializer.serialize(channel);
        final byte[] rawValue = valueSerializer.serialize(msg);
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisClusterCommands<byte[], byte[]> commands) {
                return commands.publish(rawChannel, rawValue);
            }
        });
    }
}
