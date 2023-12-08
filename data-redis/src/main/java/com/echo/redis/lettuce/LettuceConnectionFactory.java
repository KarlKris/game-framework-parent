package com.echo.redis.lettuce;

import com.echo.common.util.ClassUtils;
import com.echo.common.util.StringUtils;
import com.echo.redis.RedisSettings;
import com.echo.redis.exception.RedisUrlSyntaxException;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.resource.ClientResources;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * redis 连接
 *
 * @author: li-yuanwen
 */
@Slf4j
public class LettuceConnectionFactory {

    static final RedisCodec<byte[], byte[]> CODEC = ByteArrayCodec.INSTANCE;

    private final RedisSettings settings;
    private final ClientResources clientResources;
    private final ClientOptions clientOptions = ClientOptions.builder().timeoutOptions(TimeoutOptions.enabled()).build();

    private final AbstractRedisClient client;

    private final LettuceConnectionProvider connectionProvider;

    private StatefulConnection<byte[], byte[]> connection;

    public LettuceConnectionFactory(RedisSettings settings, ClientResources clientResources) {
        this.settings = settings;
        this.clientResources = clientResources;
        tryParseUrl();
        this.client = createClient();
        LettuceConnectionProvider provider = doCreateConnectionProvider(client, CODEC);
        if (settings.getPoolConfig() != null) {
            this.connectionProvider = new LettucePoolingConnectionProvider(provider, settings.getPoolConfig());
        } else {
            this.connectionProvider = provider;
        }
    }

    public StatefulConnection<byte[], byte[]> getConnection() {
        return getOrCreateDedicatedConnection();
    }

    public LettuceConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    public StatefulRedisPubSubConnection<byte[], byte[]> getPubSubConnection() {
        return connectionProvider.getConnection(StatefulRedisPubSubConnection.class);
    }

    public RedisClusterCommands<byte[], byte[]> getCommands() {
        StatefulConnection<byte[], byte[]> connection = getConnection();
        if (connection instanceof StatefulRedisConnection) {
            return ((StatefulRedisConnection<byte[], byte[]>) connection).sync();
        }
        if (connection instanceof StatefulRedisClusterConnection) {
            return ((StatefulRedisClusterConnection<byte[], byte[]>) connection).sync();
        }
        throw new IllegalStateException(
                String.format("%s is not a supported connection type.", connection.getClass().getName()));
    }

    public RedisClusterAsyncCommands<byte[], byte[]> getAsyncCommands() {

        StatefulConnection<byte[], byte[]> connection = getOrCreateDedicatedConnection();

        if (connection instanceof StatefulRedisConnection) {
            return ((StatefulRedisConnection<byte[], byte[]>) connection).async();
        }
        if (connection instanceof StatefulRedisClusterConnection) {
            return ((StatefulRedisClusterConnection<byte[], byte[]>) connection).async();
        }

        throw new IllegalStateException(
                String.format("%s is not a supported connection type.", connection.getClass().getName()));
    }

    protected LettuceConnectionProvider doCreateConnectionProvider(AbstractRedisClient client, RedisCodec<?, ?> codec) {
        ReadFrom readFrom = settings.getReadFrom();
        if (isCluster()) {
            return new ClusterConnectionProvider((RedisClusterClient) client, codec, readFrom);
        }
        return new StandaloneConnectionProvider((RedisClient) client, codec, readFrom);
    }

    private StatefulConnection<byte[], byte[]> getOrCreateDedicatedConnection() {
        if (connection == null) {
            connection = connectionProvider.getConnection(StatefulConnection.class);
        }
        return connection;
    }

    public void destroy() {
        if (connection != null) {
            connectionProvider.release(connection);
        }
        dispose(connectionProvider);
        try {
            Duration quietPeriod = Duration.ofMillis(100);
            Duration timeout = settings.getShutdownTimeout();
            client.shutdown(quietPeriod.toMillis(), timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {

            if (log.isWarnEnabled()) {
                log.warn((client != null ? ClassUtils.getShortName(client.getClass()) : "LettuceClient")
                        + " did not shut down gracefully.", e);
            }
        }
    }

    private void dispose(LettuceConnectionProvider connectionProvider) {
        try {
            connectionProvider.destroy();
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn(connectionProvider + " did not shut down gracefully.", e);
            }
        }
    }


    private void tryParseUrl() {
        if (!StringUtils.hasLength(settings.getUrl())) {
            return;
        }
        ConnectionInfo connectionInfo = parseUrl(settings.getUrl());
        settings.setHost(connectionInfo.getHostName());
        settings.setPort(connectionInfo.getPort());
        settings.setUserName(connectionInfo.getUsername());
        settings.setPassword(connectionInfo.getPassword());
        settings.setSsl(connectionInfo.isUseSsl());
    }


    public AbstractRedisClient createClient() {
        if (isSentinel()) {
            // 哨兵
            RedisURI redisURI = sentinelUri();
            RedisClient redisClient;
            if (clientResources != null) {
                redisClient = RedisClient.create(clientResources, redisURI);
            } else {
                redisClient = RedisClient.create(redisURI);
            }

            redisClient.setOptions(clientOptions);
            return redisClient;
        }

        if (isCluster()) {
            // 集群
            List<RedisURI> redisURIS = clusterUri();
            RedisClusterClient clusterClient;
            if (clientResources != null) {
                clusterClient = RedisClusterClient.create(clientResources, redisURIS);
            } else {
                clusterClient = RedisClusterClient.create(redisURIS);
            }

            ClusterClientOptions options = ClusterClientOptions.builder(clientOptions).build();
            if (settings.getCluster().getMaxRedirects() != null) {
                options.mutate().maxRedirects(settings.getCluster().getMaxRedirects()).build();
            }

            clusterClient.setOptions(options);
            return clusterClient;
        }

        // 单机
        RedisURI redisURI = redisURI(settings.getHost(), settings.getPort());
        RedisClient redisClient;
        if (clientResources != null) {
            redisClient = RedisClient.create(clientResources, redisURI);
        } else {
            redisClient = RedisClient.create(redisURI);
        }

        redisClient.setOptions(clientOptions);
        return redisClient;
    }

    private boolean isSentinel() {
        return settings.getSentinel() != null;
    }

    private boolean isCluster() {
        return settings.getCluster() != null;
    }

    private RedisURI sentinelUri() {
        RedisURI.Builder builder = RedisURI.builder();

        RedisSettings.Sentinel sentinel = settings.getSentinel();
        String sentinelPassword = sentinel.getPassword();
        char[] senPwd = null;
        if (StringUtils.hasLength(sentinelPassword)) {
            senPwd = sentinelPassword.toCharArray();
        }
        for (String node : sentinel.getNodes()) {
            try {
                String[] parts = StringUtils.split(node, ":");
                if (parts == null || parts.length != 2) {
                    throw new IllegalStateException("Must be defined as 'host:port'");
                }
                RedisURI.Builder sentinelBuilder = RedisURI.Builder.redis(parts[0], Integer.parseInt(parts[1]));

                if (senPwd != null) {
                    sentinelBuilder.withPassword(senPwd);
                }
                builder.withSentinel(sentinelBuilder.build());
            } catch (RuntimeException ex) {
                throw new IllegalStateException("Invalid redis sentinel property '" + node + "'", ex);
            }
        }

        String userName = settings.getUserName();
        String password = settings.getPassword();
        if (StringUtils.hasLength(userName)) {
            builder.withAuthentication(userName, password);
        } else if (StringUtils.hasLength(password)) {
            builder.withPassword(password.toCharArray());
        }

        builder.withDatabase(settings.getDatabase());
        builder.withSentinelMasterId(sentinel.getMaster());
        builder.withVerifyPeer(true);
        builder.withSsl(settings.isSsl());
        builder.withVerifyPeer(settings.isVerifyPeer());
        builder.withTimeout(settings.getTimeout());
        if (StringUtils.hasLength(settings.getClientName())) {
            builder.withClientName(settings.getClientName());
        }
        return builder.build();
    }

    private List<RedisURI> clusterUri() {
        List<RedisURI> redisURIS = new ArrayList<>();
        for (String node : settings.getCluster().getNodes()) {
            try {
                String[] parts = StringUtils.split(node, ":");
                if (parts == null || parts.length != 2) {
                    throw new IllegalStateException("Must be defined as 'host:port'");
                }
                RedisURI redisURI = redisURI(parts[0], Integer.parseInt(parts[1]));
                redisURIS.add(redisURI);
            } catch (RuntimeException ex) {
                throw new IllegalStateException("Invalid redis sentinel property '" + node + "'", ex);
            }
        }
        return redisURIS;
    }

    private RedisURI redisURI(String host, int port) {
        RedisURI.Builder builder = RedisURI.Builder.redis(host, port);
        String userName = settings.getUserName();
        String password = settings.getPassword();
        if (StringUtils.hasLength(userName)) {
            builder.withAuthentication(userName, password);
        } else if (StringUtils.hasLength(password)) {
            builder.withPassword(password.toCharArray());
        }

        builder.withDatabase(settings.getDatabase());
        builder.withVerifyPeer(true);
        builder.withSsl(settings.isSsl());
        builder.withVerifyPeer(settings.isVerifyPeer());
        if (settings.getTimeout() != null) {
            builder.withTimeout(settings.getTimeout());
        }
        if (StringUtils.hasLength(settings.getClientName())) {
            builder.withClientName(settings.getClientName());
        }

        return builder.build();
    }

    protected ConnectionInfo parseUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!"redis".equals(scheme) && !"rediss".equals(scheme)) {
                throw new RedisUrlSyntaxException(url);
            }
            boolean useSsl = ("rediss".equals(scheme));
            String username = null;
            String password = null;
            if (uri.getUserInfo() != null) {
                String candidate = uri.getUserInfo();
                int index = candidate.indexOf(':');
                if (index >= 0) {
                    username = candidate.substring(0, index);
                    password = candidate.substring(index + 1);
                } else {
                    password = candidate;
                }
            }
            return new ConnectionInfo(uri, useSsl, username, password);
        } catch (URISyntaxException ex) {
            throw new RedisUrlSyntaxException(url, ex);
        }
    }

    static class ConnectionInfo {

        private final URI uri;

        private final boolean useSsl;

        private final String username;

        private final String password;

        ConnectionInfo(URI uri, boolean useSsl, String username, String password) {
            this.uri = uri;
            this.useSsl = useSsl;
            this.username = username;
            this.password = password;
        }

        boolean isUseSsl() {
            return this.useSsl;
        }

        String getHostName() {
            return this.uri.getHost();
        }

        int getPort() {
            return this.uri.getPort();
        }

        String getUsername() {
            return this.username;
        }

        String getPassword() {
            return this.password;
        }

    }
}
