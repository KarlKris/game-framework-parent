package com.echo.redis.lettuce;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 集群模式connection
 *
 * @author: li-yuanwen
 */
public class ClusterConnectionProvider implements LettuceConnectionProvider {

    private final RedisClusterClient client;
    private final RedisCodec<?, ?> codec;
    private final Optional<ReadFrom> readFrom;

    private final Object monitor = new Object();

    private volatile boolean initialized;

    /**
     * Create new {@link ClusterConnectionProvider}.
     *
     * @param client must not be {@literal null}.
     * @param codec  must not be {@literal null}.
     */
    ClusterConnectionProvider(RedisClusterClient client, RedisCodec<?, ?> codec) {
        this(client, codec, null);
    }

    /**
     * Create new {@link ClusterConnectionProvider}.
     *
     * @param client   must not be {@literal null}.
     * @param codec    must not be {@literal null}.
     * @param readFrom can be {@literal null}.
     * @since 2.1
     */
    ClusterConnectionProvider(RedisClusterClient client, RedisCodec<?, ?> codec, ReadFrom readFrom) {

        if (client == null) {
            throw new IllegalArgumentException("Client must not be null!");
        }
        if (codec == null) {
            throw new IllegalArgumentException("Codec must not be null!");
        }

        this.client = client;
        this.codec = codec;
        this.readFrom = Optional.ofNullable(readFrom);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.lettuce.LettuceConnectionProvider#getConnectionAsync(java.lang.Class)
     */
    @Override
    public <T extends StatefulConnection<?, ?>> CompletableFuture<T> getConnectionAsync(Class<T> connectionType) {

        if (!initialized) {

            // partitions have to be initialized before asynchronous usage.
            // Needs to happen only once. Initialize eagerly if
            // blocking is not an options.
            synchronized (monitor) {
                if (!initialized) {
                    client.getPartitions();
                    initialized = true;
                }
            }
        }

        if (connectionType.equals(StatefulRedisPubSubConnection.class)
                || connectionType.equals(StatefulRedisClusterPubSubConnection.class)) {

            return client.connectPubSubAsync(codec) //
                    .thenApply(connectionType::cast);
        }

        if (StatefulRedisClusterConnection.class.isAssignableFrom(connectionType)
                || connectionType.equals(StatefulConnection.class)) {

            return client.connectAsync(codec) //
                    .thenApply(connection -> {

                        readFrom.ifPresent(connection::setReadFrom);
                        return connectionType.cast(connection);
                    });
        }

        return LettuceConnectionProvider
                .failed(new UnsupportedOperationException("Connection type " + connectionType + " not supported!"));
    }

}
