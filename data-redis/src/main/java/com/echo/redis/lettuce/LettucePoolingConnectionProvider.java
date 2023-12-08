package com.echo.redis.lettuce;

import com.echo.redis.exception.PoolException;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 池化 connection
 *
 * @author: li-yuanwen
 */
@Slf4j
public class LettucePoolingConnectionProvider implements LettuceConnectionProvider {

    private final LettuceConnectionProvider connectionProvider;
    private final GenericObjectPoolConfig poolConfig;
    private final Map<StatefulConnection<?, ?>, GenericObjectPool<StatefulConnection<?, ?>>> poolRef = new ConcurrentHashMap<>(
            32);

    private final Map<StatefulConnection<?, ?>, AsyncPool<StatefulConnection<?, ?>>> asyncPoolRef = new ConcurrentHashMap<>(
            32);
    private final Map<CompletableFuture<StatefulConnection<?, ?>>, AsyncPool<StatefulConnection<?, ?>>> inProgressAsyncPoolRef = new ConcurrentHashMap<>(
            32);
    private final Map<Class<?>, GenericObjectPool<StatefulConnection<?, ?>>> pools = new ConcurrentHashMap<>(32);
    private final Map<Class<?>, AsyncPool<StatefulConnection<?, ?>>> asyncPools = new ConcurrentHashMap<>(32);
    private final BoundedPoolConfig asyncPoolConfig;

    LettucePoolingConnectionProvider(LettuceConnectionProvider connectionProvider,
                                     GenericObjectPoolConfig poolConfig) {

        if (connectionProvider == null) {
            throw new IllegalArgumentException("ConnectionProvider must not be null!");
        }
        if (poolConfig == null) {
            throw new IllegalArgumentException("GenericObjectPoolConfig must not be null!");
        }

        this.connectionProvider = connectionProvider;
        this.poolConfig = poolConfig;
        this.asyncPoolConfig = CommonsPool2ConfigConverter.bounded(this.poolConfig);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.lettuce.LettuceConnectionProvider#getConnection(java.lang.Class)
     */
    @Override
    public <T extends StatefulConnection<?, ?>> T getConnection(Class<T> connectionType) {

        GenericObjectPool<StatefulConnection<?, ?>> pool = pools.computeIfAbsent(connectionType, poolType -> {
            return ConnectionPoolSupport.createGenericObjectPool(() -> connectionProvider.getConnection(connectionType),
                    poolConfig, false);
        });

        try {

            StatefulConnection<?, ?> connection = pool.borrowObject();

            poolRef.put(connection, pool);

            return connectionType.cast(connection);
        } catch (Exception e) {
            throw new PoolException("Could not get a resource from the pool", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.lettuce.LettuceConnectionProvider#getConnectionAsync(java.lang.Class)
     */
    @Override
    public <T extends StatefulConnection<?, ?>> CompletionStage<T> getConnectionAsync(Class<T> connectionType) {

        AsyncPool<StatefulConnection<?, ?>> pool = asyncPools.computeIfAbsent(connectionType, poolType -> {

            return AsyncConnectionPoolSupport.createBoundedObjectPool(
                    () -> connectionProvider.getConnectionAsync(connectionType).thenApply(connectionType::cast), asyncPoolConfig,
                    false);
        });

        CompletableFuture<StatefulConnection<?, ?>> acquire = pool.acquire();

        inProgressAsyncPoolRef.put(acquire, pool);
        return acquire.whenComplete((connection, e) -> {

            inProgressAsyncPoolRef.remove(acquire);

            if (connection != null) {
                asyncPoolRef.put(connection, pool);
            }
        }).thenApply(connectionType::cast);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.lettuce.LettuceConnectionProvider#release(io.lettuce.core.api.StatefulConnection)
     */
    @Override
    public void release(StatefulConnection<?, ?> connection) {

        GenericObjectPool<StatefulConnection<?, ?>> pool = poolRef.remove(connection);

        if (pool == null) {

            AsyncPool<StatefulConnection<?, ?>> asyncPool = asyncPoolRef.remove(connection);

            if (asyncPool == null) {
                throw new PoolException("Returned connection " + connection
                        + " was either previously returned or does not belong to this connection provider");
            }

            discardIfNecessary(connection);
            asyncPool.release(connection).join();
            return;
        }

        discardIfNecessary(connection);
        pool.returnObject(connection);
    }

    private void discardIfNecessary(StatefulConnection<?, ?> connection) {

        if (connection instanceof StatefulRedisConnection) {

            StatefulRedisConnection<?, ?> redisConnection = (StatefulRedisConnection<?, ?>) connection;
            if (redisConnection.isMulti()) {
                redisConnection.async().discard();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.lettuce.LettuceConnectionProvider#releaseAsync(io.lettuce.core.api.StatefulConnection)
     */
    @Override
    public CompletableFuture<Void> releaseAsync(StatefulConnection<?, ?> connection) {

        GenericObjectPool<StatefulConnection<?, ?>> blockingPool = poolRef.remove(connection);

        if (blockingPool != null) {

            log.warn("Releasing asynchronously a connection that was obtained from a non-blocking pool");
            blockingPool.returnObject(connection);
            return CompletableFuture.completedFuture(null);
        }

        AsyncPool<StatefulConnection<?, ?>> pool = asyncPoolRef.remove(connection);

        if (pool == null) {
            return LettuceConnectionProvider.failed(new PoolException("Returned connection " + connection
                    + " was either previously returned or does not belong to this connection provider"));
        }

        return pool.release(connection);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    @Override
    public void destroy() throws Exception {

        List<CompletableFuture<?>> futures = new ArrayList<>();
        if (!poolRef.isEmpty() || !asyncPoolRef.isEmpty()) {
            log.warn("LettucePoolingConnectionProvider contains unreleased connections");
        }

        if (!inProgressAsyncPoolRef.isEmpty()) {

            log.warn("LettucePoolingConnectionProvider has active connection retrievals");
            inProgressAsyncPoolRef.forEach((k, v) -> futures.add(k.thenApply(StatefulConnection::closeAsync)));
        }

        if (!poolRef.isEmpty()) {

            poolRef.forEach((connection, pool) -> pool.returnObject(connection));
            poolRef.clear();
        }

        if (!asyncPoolRef.isEmpty()) {

            asyncPoolRef.forEach((connection, pool) -> futures.add(pool.release(connection)));
            asyncPoolRef.clear();
        }

        pools.forEach((type, pool) -> pool.close());

        CompletableFuture
                .allOf(futures.stream().map(it -> it.exceptionally(LettuceConnectionProvider.ignoreErrors()))
                        .toArray(CompletableFuture[]::new)) //
                .thenCompose(ignored -> {

                    CompletableFuture[] poolClose = asyncPools.values().stream().map(AsyncPool::closeAsync)
                            .map(it -> it.exceptionally(LettuceConnectionProvider.ignoreErrors())).toArray(CompletableFuture[]::new);

                    return CompletableFuture.allOf(poolClose);
                }) //
                .thenRun(() -> {
                    asyncPoolRef.clear();
                    inProgressAsyncPoolRef.clear();
                }) //
                .join();

        pools.clear();
    }
}
