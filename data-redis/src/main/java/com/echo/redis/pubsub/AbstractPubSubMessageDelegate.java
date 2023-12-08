package com.echo.redis.pubsub;


import com.echo.common.util.TypeParameterMatcher;
import com.echo.redis.core.RedisSerializer;

/**
 * redis 发布/订阅消息处理 基类
 *
 * @author: li-yuanwen
 */
public abstract class AbstractPubSubMessageDelegate<B> implements MessageListener {

    private final Class<B> bodyClass;
    private final RedisSerializer valueSerializer;
    private final RedisMessageListenerContainer container;

    public AbstractPubSubMessageDelegate(RedisMessageListenerContainer container, RedisSerializer valueSerializer) {
        this.bodyClass = (Class<B>) TypeParameterMatcher.findGenericsClass(this, AbstractPubSubMessageDelegate.class, "B");
        this.container = container;
        this.valueSerializer = valueSerializer;
    }


    @Override
    public void onMessage(DefaultMessage message, byte[] pattern) {
        B body = valueSerializer.deserialize(message.getBody(), bodyClass);
        handleMessage(body);
    }


    public void registerListener() {
        container.addMessageListener(this, new ChannelTopic(getChannel()));
    }

    public void destroy() {
        container.removeMessageListener(this);
    }

    /**
     * redis订阅channel
     *
     * @return PubSubChannel
     */
    public abstract String getChannel();

    /**
     * redis订阅内容处理
     *
     * @param body 订阅内容
     */
    public abstract void handleMessage(B body);

}
