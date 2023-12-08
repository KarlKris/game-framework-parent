package com.echo.redis;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisURI;
import lombok.experimental.Accessors;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;
import java.util.List;

/**
 * redis 相关配置
 *
 * @author: li-yuanwen
 */
@Accessors
public class RedisSettings {

    /**
     * Connection URL.
     * redis://user:password@example.com:6379
     */
    private String url;

    /**
     * 主机地址
     **/
    private String host = "localhost";
    /**
     * 端口
     **/
    private int port = 6379;

    /**
     * ssl
     **/
    private boolean ssl;
    /**
     * peer verification
     **/
    private boolean verifyPeer = true;
    /**
     * Enable Start TLS to send the first bytes unencrypted.
     **/
    private boolean startTls;
    /**
     * 用户
     **/
    private String userName;
    /**
     * 密码
     **/
    private String password;
    /**
     * 数据库
     **/
    private int database;

    /**
     * Client name to be set on connections with CLIENT SETNAME.
     **/
    private String clientName;

    /**
     * command timeout
     **/
    private Duration commandTimeout = Duration.ofSeconds(RedisURI.DEFAULT_TIMEOUT);
    /**
     * Shutdown timeout
     **/
    private Duration shutdownTimeout = Duration.ofMillis(100);
    /**
     * 连接池配置
     **/
    private Pool pool;

    /**
     * Read timeout.
     **/
    private Duration timeout = Duration.ofSeconds(RedisURI.DEFAULT_TIMEOUT);
    /**
     * Connection timeout.
     **/
    private Duration connectTimeout;

    /**
     * 哨兵配置
     **/
    private Sentinel sentinel;
    /**
     * 集群配置
     **/
    private Cluster cluster;
    /**
     * readfrom
     **/
    private ReadFrom readFrom;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public Duration getShutdownTimeout() {
        return shutdownTimeout;
    }

    public void setShutdownTimeout(Duration shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public boolean isVerifyPeer() {
        return verifyPeer;
    }

    public void setVerifyPeer(boolean verifyPeer) {
        this.verifyPeer = verifyPeer;
    }

    public boolean isStartTls() {
        return startTls;
    }

    public void setStartTls(boolean startTls) {
        this.startTls = startTls;
    }

    public Sentinel getSentinel() {
        return sentinel;
    }

    public Duration getCommandTimeout() {
        return commandTimeout;
    }

    public void setCommandTimeout(Duration commandTimeout) {
        this.commandTimeout = commandTimeout;
    }

    public void setSentinel(Sentinel sentinel) {
        this.sentinel = sentinel;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public ReadFrom getReadFrom() {
        return readFrom;
    }

    public void setReadFrom(ReadFrom readFrom) {
        this.readFrom = readFrom;
    }

    public GenericObjectPoolConfig getPoolConfig() {
        if (pool == null) {
            return null;
        }
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(pool.getMaxIdle());
        poolConfig.setMinIdle(pool.getMinIdle());
        poolConfig.setMaxIdle(pool.getMaxIdle());
        poolConfig.setMaxTotal(pool.getMaxActive());
        poolConfig.setMaxWait(pool.getMaxWait());
        poolConfig.setTimeBetweenEvictionRuns(pool.getTimeBetweenEvictionRuns());
        return poolConfig;
    }

    /**
     * Cluster properties.
     */
    public static class Cluster {

        /**
         * Comma-separated list of "host:port" pairs to bootstrap from. This represents an
         * "initial" list of cluster nodes and is required to have at least one entry.
         */
        private List<String> nodes;

        /**
         * Maximum number of redirects to follow when executing commands across the
         * cluster.
         */
        private Integer maxRedirects;

        public List<String> getNodes() {
            return this.nodes;
        }

        public void setNodes(List<String> nodes) {
            this.nodes = nodes;
        }

        public Integer getMaxRedirects() {
            return this.maxRedirects;
        }

        public void setMaxRedirects(Integer maxRedirects) {
            this.maxRedirects = maxRedirects;
        }

    }

    /**
     * Redis sentinel properties.
     */
    public static class Sentinel {

        /**
         * Name of the Redis server.
         */
        private String master;

        /**
         * Comma-separated list of "host:port" pairs.
         */
        private List<String> nodes;

        /**
         * Password for authenticating with sentinel(s).
         */
        private String password;

        public String getMaster() {
            return this.master;
        }

        public void setMaster(String master) {
            this.master = master;
        }

        public List<String> getNodes() {
            return this.nodes;
        }

        public void setNodes(List<String> nodes) {
            this.nodes = nodes;
        }

        public String getPassword() {
            return this.password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }


    /**
     * 连接池配置
     */
    public static class Pool {

        /**
         * Maximum number of "idle" connections in the pool. Use a negative value to
         * indicate an unlimited number of idle connections.
         */
        private int maxIdle = 8;

        /**
         * Target for the minimum number of idle connections to maintain in the pool. This
         * setting only has an effect if both it and time between eviction runs are
         * positive.
         */
        private int minIdle = 0;

        /**
         * Maximum number of connections that can be allocated by the pool at a given
         * time. Use a negative value for no limit.
         */
        private int maxActive = 8;

        /**
         * Maximum amount of time a connection allocation should block before throwing an
         * exception when the pool is exhausted. Use a negative value to block
         * indefinitely.
         */
        private Duration maxWait = Duration.ofMillis(-1);

        /**
         * Time between runs of the idle object evictor thread. When positive, the idle
         * object evictor thread starts, otherwise no idle object eviction is performed.
         */
        private Duration timeBetweenEvictionRuns;

        public int getMaxIdle() {
            return this.maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return this.minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public int getMaxActive() {
            return this.maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public Duration getMaxWait() {
            return this.maxWait;
        }

        public void setMaxWait(Duration maxWait) {
            this.maxWait = maxWait;
        }

        public Duration getTimeBetweenEvictionRuns() {
            return this.timeBetweenEvictionRuns;
        }

        public void setTimeBetweenEvictionRuns(Duration timeBetweenEvictionRuns) {
            this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
        }

    }

    public static class Refresh {

        /**
         * Whether to discover and query all cluster nodes for obtaining the
         * cluster topology. When set to false, only the initial seed nodes are
         * used as sources for topology discovery.
         */
        private boolean dynamicRefreshSources = true;

        /**
         * Cluster topology refresh period.
         */
        private Duration period;

        /**
         * Whether adaptive topology refreshing using all available refresh
         * triggers should be used.
         */
        private boolean adaptive;

        public boolean isDynamicRefreshSources() {
            return this.dynamicRefreshSources;
        }

        public void setDynamicRefreshSources(boolean dynamicRefreshSources) {
            this.dynamicRefreshSources = dynamicRefreshSources;
        }

        public Duration getPeriod() {
            return this.period;
        }

        public void setPeriod(Duration period) {
            this.period = period;
        }

        public boolean isAdaptive() {
            return this.adaptive;
        }

        public void setAdaptive(boolean adaptive) {
            this.adaptive = adaptive;
        }

    }


}

