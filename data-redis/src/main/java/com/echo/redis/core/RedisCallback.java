package com.echo.redis.core;

import io.lettuce.core.cluster.api.sync.RedisClusterCommands;

public interface RedisCallback<T> {


    T doInRedis(RedisClusterCommands<byte[], byte[]> commands);

}
