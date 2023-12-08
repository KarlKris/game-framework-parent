package com.echo.redis.lettuce;

import com.echo.common.util.ReflectionUtils;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.sentinel.api.StatefulRedisSentinelConnection;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 单机,哨兵模式Connection
 *
 * @author: li-yuanwen
 */
public class StandaloneConnectionProvider implements LettuceConnectionProvider {

    private final RedisClient client;
    private final RedisCodec<?, ?> codec;
    private final Optional<ReadFrom> readFrom;
    private final Supplier<RedisURI> redisURISupplier;


    /**
     * Create new {@link StandaloneConnectionProvider}.
     *
     * @param client must not be {@literal null}.
     * @param codec  must not be {@literal null}.
     */
    StandaloneConnectionProvider(RedisClient client, RedisCodec<?, ?> codec) {
        this(client, codec, null);
    }

    /**
     * Create new {@link StandaloneConnectionProvider}.
     *
     * @param client   must not be {@literal null}.
     * @param codec    must not be {@literal null}.
     * @param readFrom can be {@literal null}.
     * @since 2.1
     */
    StandaloneConnectionProvider(RedisClient client, RedisCodec<?, ?> codec, ReadFrom readFrom) {

        this.client = client;
        this.codec = codec;
        this.readFrom = Optional.ofNullable(readFrom);

        redisURISupplier = new Supplier<RedisURI>() {

            AtomicReference<RedisURI> uriFieldReference = new AtomicReference<>();

            @Override
            public RedisURI get() {

                RedisURI uri = uriFieldReference.get();
                if (uri != null) {
                    return uri;
                }

                uri = (RedisURI) ReflectionUtils.getField(ReflectionUtils.getField(RedisClient.class, "redisURI"), client);

                return uriFieldReference.compareAndSet(null, uri) ? uri : uriFieldReference.get();
            }
        };
    }

    @Override
    public <T extends StatefulConnection<?, ?>> T getConnection(Class<T> connectionType) {

        if (connectionType.equals(StatefulRedisSentinelConnection.class)) {
            return connectionType.cast(client.connectSentinel());
        }

        if (connectionType.equals(StatefulRedisPubSubConnection.class)) {
            return connectionType.cast(client.connectPubSub(codec));
        }

        if (StatefulConnection.class.isAssignableFrom(connectionType)) {

            return connectionType.cast(readFrom.map(it -> this.masterReplicaConnection(redisURISupplier.get(), it))
                    .orElseGet(() -> client.connect(codec)));
        }

        throw new UnsupportedOperationException("Connection type " + connectionType + " not supported!");
    }

    @Override
    public <T extends StatefulConnection<?, ?>> CompletionStage<T> getConnectionAsync(Class<T> connectionType) {
        return getConnectionAsync(connectionType, redisURISupplier.get());
    }

    @SuppressWarnings({"null", "unchecked", "rawtypes"})
    public <T extends StatefulConnection<?, ?>> CompletionStage<T> getConnectionAsync(Class<T> connectionType,
                                                                                      RedisURI redisURI) {

        if (connectionType.equals(StatefulRedisSentinelConnection.class)) {
            return client.connectSentinelAsync(StringCodec.UTF8, redisURI).thenApply(connectionType::cast);
        }

        if (connectionType.equals(StatefulRedisPubSubConnection.class)) {
            return client.connectPubSubAsync(codec, redisURI).thenApply(connectionType::cast);
        }

        if (StatefulConnection.class.isAssignableFrom(connectionType)) {
            return readFrom.map(it -> this.masterReplicaConnectionAsync(redisURI, it)) //
                    .orElseGet(() -> (CompletionStage) client.connectAsync(codec, redisURI)) //
                    .thenApply(connectionType::cast);
        }

        return LettuceConnectionProvider.failed(new UnsupportedOperationException("Connection type " + connectionType + " not supported!"));
    }


    private StatefulRedisConnection masterReplicaConnection(RedisURI redisUri, ReadFrom readFrom) {

        StatefulRedisMasterReplicaConnection<?, ?> connection = MasterReplica.connect(client, codec, redisUri);
        connection.setReadFrom(readFrom);

        return connection;
    }

    private CompletionStage<StatefulRedisConnection<?, ?>> masterReplicaConnectionAsync(RedisURI redisUri,
                                                                                        ReadFrom readFrom) {

        CompletableFuture<? extends StatefulRedisMasterReplicaConnection<?, ?>> connection = MasterReplica
                .connectAsync(client,
                        codec, redisUri);

        return connection.thenApply(conn -> {

            conn.setReadFrom(readFrom);

            return conn;
        });
    }
}
