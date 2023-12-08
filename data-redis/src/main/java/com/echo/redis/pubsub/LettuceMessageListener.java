package com.echo.redis.pubsub;

import io.lettuce.core.pubsub.RedisPubSubListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @author: li-yuanwen
 */
@Slf4j
public class LettuceMessageListener implements RedisPubSubListener<byte[], byte[]> {

    private final MessageListener listener;

    public LettuceMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void message(byte[] channel, byte[] message) {
        listener.onMessage(new DefaultMessage(channel, message), null);
    }

    @Override
    public void message(byte[] pattern, byte[] channel, byte[] message) {
        listener.onMessage(new DefaultMessage(channel, message), pattern);
    }

    @Override
    public void subscribed(byte[] channel, long count) {
        if (log.isDebugEnabled()) {
            log.debug("subscribed {}, count: {}", Arrays.toString(channel), count);
        }
    }

    @Override
    public void psubscribed(byte[] pattern, long count) {
        if (log.isDebugEnabled()) {
            log.debug("psubscribed {}, count: {}", Arrays.toString(pattern), count);
        }
    }

    @Override
    public void unsubscribed(byte[] channel, long count) {
        if (log.isDebugEnabled()) {
            log.debug("unsubscribed {}, count: {}", Arrays.toString(channel), count);
        }
    }

    @Override
    public void punsubscribed(byte[] pattern, long count) {
        if (log.isDebugEnabled()) {
            log.debug("punsubscribed {}, count: {}", Arrays.toString(pattern), count);
        }
    }
}
