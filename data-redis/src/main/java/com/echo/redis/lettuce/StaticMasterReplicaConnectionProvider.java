package com.echo.redis.lettuce;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * 主从复制模式connection
 *
 * @author: li-yuanwen
 */
public class StaticMasterReplicaConnectionProvider implements LettuceConnectionProvider {

    private final RedisClient client;
    private final RedisCodec<?, ?> codec;
    private final Optional<ReadFrom> readFrom;
    private final Collection<RedisURI> nodes;

    /**
     * Create new {@link StaticMasterReplicaConnectionProvider}.
     *
     * @param client   must not be {@literal null}.
     * @param codec    must not be {@literal null}.
     * @param nodes    must not be {@literal null}.
     * @param readFrom can be {@literal null}.
     */
    StaticMasterReplicaConnectionProvider(RedisClient client, RedisCodec<?, ?> codec, Collection<RedisURI> nodes,
                                          ReadFrom readFrom) {

        this.client = client;
        this.codec = codec;
        this.readFrom = Optional.ofNullable(readFrom);
        this.nodes = nodes;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.lettuce.LettuceConnectionProvider#getConnection(java.lang.Class)
     */
    @Override
    public <T extends StatefulConnection<?, ?>> T getConnection(Class<T> connectionType) {

        if (connectionType.equals(StatefulRedisPubSubConnection.class)) {
            throw new UnsupportedOperationException("Pub/Sub connections not supported with Master/Replica configurations");
        }

        if (StatefulConnection.class.isAssignableFrom(connectionType)) {

            StatefulRedisMasterReplicaConnection<?, ?> connection = MasterReplica.connect(client, codec, nodes);
            readFrom.ifPresent(connection::setReadFrom);

            return connectionType.cast(connection);
        }

        throw new UnsupportedOperationException(String.format("Connection type %s not supported!", connectionType));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.lettuce.LettuceConnectionProvider#getConnectionAsync(java.lang.Class)
     */
    @Override
    public <T extends StatefulConnection<?, ?>> CompletionStage<T> getConnectionAsync(Class<T> connectionType) {

        if (StatefulConnection.class.isAssignableFrom(connectionType)) {

            CompletableFuture<? extends StatefulRedisMasterReplicaConnection<?, ?>> connection = MasterReplica
                    .connectAsync(client, codec, nodes);

            connection.thenApply(it -> {

                readFrom.ifPresent(readFrom -> it.setReadFrom(readFrom));
                return connectionType.cast(connection);
            });
        }

        throw new UnsupportedOperationException(String.format("Connection type %s not supported!", connectionType));
    }

}
