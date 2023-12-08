package com.echo.redis.pubsub;

/**
 * @author: li-yuanwen
 */
public interface MessageListener {


    /**
     * Callback for processing received objects through Redis.
     *
     * @param message message must not be {@literal null}.
     * @param pattern pattern matching the channel (if specified) - can be {@literal null}.
     */
    void onMessage(DefaultMessage message, byte[] pattern);

}
