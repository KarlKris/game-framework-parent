package com.echo;

import com.echo.redis.core.RedisSerializer;
import com.echo.redis.pubsub.AbstractPubSubMessageDelegate;
import com.echo.redis.pubsub.RedisMessageListenerContainer;

/**
 * @author: li-yuanwen
 */
public class SimpleMessageListener extends AbstractPubSubMessageDelegate<Message> {

    public SimpleMessageListener(RedisMessageListenerContainer container, RedisSerializer valueSerializer) {
        super(container, valueSerializer);
    }

    @Override
    public String getChannel() {
        return "channel:message";
    }

    @Override
    public void handleMessage(Message body) {
        System.out.println(body.getMsg());
    }
}
