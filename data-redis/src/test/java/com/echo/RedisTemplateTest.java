package com.echo;

import com.echo.redis.RedisSettings;
import com.echo.redis.core.ProtobufRedisSerializer;
import com.echo.redis.core.RedisTemplate;
import com.echo.redis.lettuce.LettuceConnectionFactory;
import com.echo.redis.pubsub.RedisMessageListenerContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.push.PushListener;
import io.lettuce.core.api.push.PushMessage;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.lettuce.core.resource.ClientResources;
import org.junit.Test;

/**
 * RedisTemplate Test
 *
 * @author: li-yuanwen
 */
public class RedisTemplateTest {

    @Test
    public void test() throws InterruptedException {
        RedisSettings redisSettings = new RedisSettings();
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisSettings, ClientResources.builder().build());
        ProtobufRedisSerializer serializer = new ProtobufRedisSerializer();

        RedisMessageListenerContainer container = new RedisMessageListenerContainer(connectionFactory, serializer);
        SimpleMessageListener listener = new SimpleMessageListener(container, serializer);
        listener.registerListener();
        Thread.sleep(10000);
    }

    @Test
    public void test2() throws InterruptedException {
        RedisSettings redisSettings = new RedisSettings();
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisSettings, ClientResources.builder().build());
        ProtobufRedisSerializer serializer = new ProtobufRedisSerializer();

        RedisMessageListenerContainer container = new RedisMessageListenerContainer(connectionFactory, serializer);
        SimpleMessageListener listener = new SimpleMessageListener(container, serializer);
        listener.registerListener();
        Thread.sleep(10000);
    }

    @Test
    public void test3() throws InterruptedException {
        RedisSettings redisSettings = new RedisSettings();
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisSettings, ClientResources.builder().build());
        ProtobufRedisSerializer serializer = new ProtobufRedisSerializer();
        RedisTemplate redisTemplate = new RedisTemplate(serializer, connectionFactory);

        String value = "hello world!";
        Message message = new Message();
        message.setMsg(value);
//        boolean set = redisTemplate.set("testProtobuf", message);
//        System.out.println(set);
//
//        Message response = redisTemplate.get("testProtobuf", Message.class);
//        System.out.println(message.equals(response));

        long publish = redisTemplate.publish("channel:message", message);
        System.out.println(publish);

        Thread.sleep(5000);
    }

    @Test
    public void test4() throws InterruptedException {
        ProtobufRedisSerializer serializer = new ProtobufRedisSerializer();
        RedisClient client = RedisClient.create("redis://127.0.0.1");
        StatefulRedisPubSubConnection<String, String> connection = client.connectPubSub();
        connection.addListener(new PushListener() {
            @Override
            public void onPushMessage(PushMessage message) {
                System.out.println(message.getType());
            }
        });

        RedisPubSubAsyncCommands<String, String> async = connection.async();
        async.subscribe("channel:message");

        Thread.sleep(5000);
    }

}
