package com.echo.redis.pubsub;

import com.echo.common.util.ByteArrayWrapper;
import com.echo.common.util.CollectionUtils;
import com.echo.redis.core.RedisSerializer;
import com.echo.redis.lettuce.LettuceConnectionFactory;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

/**
 * redis pub/sub 监听器容器
 *
 * @author: li-yuanwen
 */
@Slf4j
public class RedisMessageListenerContainer implements Runnable {

    // lookup map between patterns and listeners
    private final Map<ByteArrayWrapper, Collection<MessageListener>> patternMapping = new ConcurrentHashMap<>();
    // lookup map between channels and listeners
    private final Map<ByteArrayWrapper, Collection<MessageListener>> channelMapping = new ConcurrentHashMap<>();
    // lookup map between listeners and channels
    private final Map<MessageListener, Set<Topic>> listenerTopics = new ConcurrentHashMap<>();

    private final Object monitor = new Object();
    // whether the container has been initialized
    private volatile boolean initialized = false;
    // whether the container uses a connection or not
    // (as the container might be running but w/o listeners, it won't use any resources)
    private volatile boolean listening = false;

    private ThreadPoolExecutor executor;

    private final LettuceConnectionFactory connectionFactory;
    private final StatefulRedisPubSubConnection<byte[], byte[]> pubSubConnection;
    private final RedisSerializer keySerializer;

    private final LettuceMessageListener listener = new LettuceMessageListener(new DispatchMessageListener());

    public RedisMessageListenerContainer(LettuceConnectionFactory connectionFactory, RedisSerializer keySerializer) {
        this.connectionFactory = connectionFactory;
        this.pubSubConnection = connectionFactory.getPubSubConnection();
        this.keySerializer = keySerializer;
        this.executor = new ThreadPoolExecutor(1, 1, 0
                , TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public void addMessageListener(MessageListener listener, Topic topic) {
        addMessageListener(listener, Collections.singletonList(topic));
    }

    public void addMessageListener(MessageListener listener, Collection<? extends Topic> topics) {
        addListener(listener, topics);
        lazyListen();
    }

    public void removeMessageListener(MessageListener listener) {
        removeMessageListener(listener, Collections.emptyList());
    }

    public void removeMessageListener(MessageListener listener, Topic topic) {
        removeMessageListener(listener, Collections.singletonList(topic));
    }

    public void removeMessageListener(MessageListener listener, Collection<? extends Topic> topics) {
        removeListener(listener, topics);
    }


    private void addListener(MessageListener listener, Collection<? extends Topic> topics) {

        List<byte[]> channels = new ArrayList<>(topics.size());
        List<byte[]> patterns = new ArrayList<>(topics.size());

        boolean trace = log.isTraceEnabled();

        // add listener mapping
        Set<Topic> set = listenerTopics.get(listener);
        if (set == null) {
            set = new CopyOnWriteArraySet<>();
            listenerTopics.put(listener, set);
        }
        set.addAll(topics);

        for (Topic topic : topics) {

            ByteArrayWrapper holder = new ByteArrayWrapper(keySerializer.serialize(topic.getTopic()));

            if (topic instanceof ChannelTopic) {
                Collection<MessageListener> collection = channelMapping.get(holder);
                if (collection == null) {
                    collection = new CopyOnWriteArraySet<>();
                    channelMapping.put(holder, collection);
                }
                collection.add(listener);
                channels.add(holder.getArray());

                if (trace)
                    log.trace("Adding listener '" + listener + "' on channel '" + topic.getTopic() + "'");
            } else if (topic instanceof PatternTopic) {
                Collection<MessageListener> collection = patternMapping.get(holder);
                if (collection == null) {
                    collection = new CopyOnWriteArraySet<>();
                    patternMapping.put(holder, collection);
                }
                collection.add(listener);
                patterns.add(holder.getArray());

                if (trace)
                    log.trace("Adding listener '" + listener + "' for pattern '" + topic.getTopic() + "'");
            } else {
                throw new IllegalArgumentException("Unknown topic type '" + topic.getClass() + "'");
            }
        }

        if (!listening) {
            return;
        }

        RedisPubSubAsyncCommands<byte[], byte[]> commands = pubSubConnection.async();
        if (!channels.isEmpty()) {
            commands.subscribe(channels.toArray(new byte[channels.size()][]));
        }
        if (!patterns.isEmpty()) {
            commands.psubscribe(patterns.toArray(new byte[patterns.size()][]));
        }
    }

    private void removeListener(MessageListener listener, Collection<? extends Topic> topics) {
        boolean trace = log.isTraceEnabled();

        // check stop listening case
        if (listener == null && CollectionUtils.isEmpty(topics)) {
            cancel();
        }

        List<byte[]> channelsToRemove = new ArrayList<>();
        List<byte[]> patternsToRemove = new ArrayList<>();

        // check unsubscribe all topics case
        if (CollectionUtils.isEmpty(topics)) {
            Set<Topic> set = listenerTopics.remove(listener);
            // listener not found, bail out
            if (set == null) {
                return;
            }
            topics = set;
        }

        for (Topic topic : topics) {
            ByteArrayWrapper holder = new ByteArrayWrapper(keySerializer.serialize(topic.getTopic()));

            if (topic instanceof ChannelTopic) {
                remove(listener, topic, holder, channelMapping, channelsToRemove);

                if (trace) {
                    String msg = (listener != null ? "listener '" + listener + "'" : "all listeners");
                    log.trace("Removing " + msg + " from channel '" + topic.getTopic() + "'");
                }
            } else if (topic instanceof PatternTopic) {
                remove(listener, topic, holder, patternMapping, patternsToRemove);

                if (trace) {
                    String msg = (listener != null ? "listener '" + listener + "'" : "all listeners");
                    log.trace("Removing " + msg + " from pattern '" + topic.getTopic() + "'");
                }
            }
        }

        // double check whether there are still subscriptions available otherwise cancel the connection
        // as most drivers forfeit the connection on unsubscribe
        if (listenerTopics.isEmpty()) {
            cancel();
        }

        // check the current listening state
        else if (listening) {

            RedisPubSubAsyncCommands<byte[], byte[]> asyncCommands = pubSubConnection.async();
            asyncCommands.unsubscribe(channelsToRemove.toArray(new byte[channelsToRemove.size()][]));
            asyncCommands.punsubscribe(patternsToRemove.toArray(new byte[patternsToRemove.size()][]));
        }
    }

    private void remove(MessageListener listener, Topic topic, ByteArrayWrapper holder,
                        Map<ByteArrayWrapper, Collection<MessageListener>> mapping, List<byte[]> topicToRemove) {

        Collection<MessageListener> listeners = mapping.get(holder);
        Collection<MessageListener> listenersToRemove = null;

        if (listeners != null) {
            // remove only one listener
            if (listener != null) {
                listeners.remove(listener);
                listenersToRemove = Collections.singletonList(listener);
            }

            // no listener given - remove all of them
            else {
                listenersToRemove = listeners;
            }

            // start removing listeners
            for (MessageListener messageListener : listenersToRemove) {
                Set<Topic> topics = listenerTopics.get(messageListener);
                if (topics != null) {
                    topics.remove(topic);
                }
                if (CollectionUtils.isEmpty(topics)) {
                    listenerTopics.remove(messageListener);
                }
            }
            // if we removed everything, remove the empty holder collection
            if (listener == null || listeners.isEmpty()) {
                mapping.remove(holder);
                topicToRemove.add(holder.getArray());
            }
        }
    }

    private void cancel() {
        if (!listening || pubSubConnection == null) {
            return;
        }

        listening = false;

        if (log.isTraceEnabled()) {
            log.trace("Cancelling Redis subscription...");
        }

        pubSubConnection.removeListener(listener);
        connectionFactory.getConnectionProvider().release(pubSubConnection);
    }

    private void lazyListen() {
        boolean debug = log.isDebugEnabled();
        boolean started = false;

        if (!listening) {
            synchronized (monitor) {
                if (!listening) {
                    if (channelMapping.size() > 0 || patternMapping.size() > 0) {
                        executor.execute(this);
                        listening = true;
                        started = true;
                    }
                }
            }
            if (debug) {
                if (started) {
                    log.debug("Started listening for Redis messages");
                } else {
                    log.debug("Postpone listening for Redis messages until actual listeners are added");
                }
            }
        }
    }

    @Override
    public void run() {
        if (initialized) {
            return;
        }
        initialized = true;
        this.pubSubConnection.addListener(listener);
        if (channelMapping.isEmpty()) {
            pubSubConnection.sync().psubscribe(unwrap(patternMapping.keySet()));
        } else {
            if (!patternMapping.isEmpty()) {
                pubSubConnection.sync().psubscribe(unwrap(patternMapping.keySet()));
            }
            pubSubConnection.sync().subscribe(unwrap(channelMapping.keySet()));
        }
        System.out.println("Started listening for Redis messages");
    }

    private byte[][] unwrap(Collection<ByteArrayWrapper> holders) {
        if (CollectionUtils.isEmpty(holders)) {
            return new byte[0][];
        }

        byte[][] unwrapped = new byte[holders.size()][];

        int index = 0;
        for (ByteArrayWrapper arrayHolder : holders) {
            unwrapped[index++] = arrayHolder.getArray();
        }

        return unwrapped;
    }

    private class DispatchMessageListener implements MessageListener {

        @Override
        public void onMessage(DefaultMessage message, byte[] pattern) {
            Collection<MessageListener> listeners = null;

            // if it's a pattern, disregard channel
            if (pattern != null && pattern.length > 0) {
                listeners = patternMapping.get(new ByteArrayWrapper(pattern));
            } else {
                pattern = null;
                // do channel matching first
                listeners = channelMapping.get(new ByteArrayWrapper(message.getChannel()));
            }

            if (!CollectionUtils.isEmpty(listeners)) {
                dispatchMessage(listeners, message, pattern);
            }
        }
    }

    private void dispatchMessage(Collection<MessageListener> listeners, DefaultMessage message, byte[] pattern) {

        byte[] source = (pattern != null ? pattern.clone() : message.getChannel());

        for (MessageListener messageListener : listeners) {
            executor.execute(() -> processMessage(messageListener, message, source));
        }
    }

    protected void processMessage(MessageListener listener, DefaultMessage message, byte[] pattern) {
        executeListener(listener, message, pattern);
    }

    /**
     * Execute the specified listener.
     */
    protected void executeListener(MessageListener listener, DefaultMessage message, byte[] pattern) {
        try {
            listener.onMessage(message, pattern);
        } catch (Throwable ex) {
            log.error("", ex);
        }
    }
}
