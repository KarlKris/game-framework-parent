package com.echo.redis.core;

public interface RedisPubSubOperations {

    /**
     * 发布消息
     *
     * @param channel the channel to publish to, must not be null
     * @param msg     消息
     */
    long publish(final String channel, final Object msg);

}
