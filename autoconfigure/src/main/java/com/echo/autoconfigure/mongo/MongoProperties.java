package com.echo.autoconfigure.mongo;

import com.echo.ioc.anno.ConfigurationProperties;
import com.mongodb.ConnectionString;
import org.bson.UuidRepresentation;

/**
 * mongodb 配置文件类
 *
 * @author: li-yuanwen
 */
@ConfigurationProperties(prefix = "mongodb")
public class MongoProperties {

    /**
     * Default port used when the configured port is {@code null}.
     */
    public static final int DEFAULT_PORT = 27017;

    /**
     * Default URI used when the configured URI is {@code null}.
     */
    public static final String DEFAULT_URI = "mongodb://localhost/test";

    /**
     * Mongo server host. Cannot be set with URI.
     */
    private String host;

    /**
     * Mongo server port. Cannot be set with URI.
     */
    private Integer port = null;

    /**
     * Mongo database URI. Cannot be set with host, port, credentials and replica set
     * name.
     */
    private String uri;

    /**
     * Database name.
     */
    private String database;

    /**
     * Authentication database name.
     */
    private String authenticationDatabase;

    /**
     * Login user of the mongo server. Cannot be set with URI.
     */
    private String username;

    /**
     * Login password of the mongo server. Cannot be set with URI.
     */
    private char[] password;

    /**
     * Required replica set name for the cluster. Cannot be set with URI.
     */
    private String replicaSetName;

    /**
     * Representation to use when converting a UUID to a BSON binary value.
     */
    private UuidRepresentation uuidRepresentation = UuidRepresentation.JAVA_LEGACY;

    /**
     * Whether to enable auto-index creation.
     */
    private boolean autoIndexCreation = false;

    /**
     * scan packages for entity
     */
    private String entityPackage;

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDatabase() {
        return this.database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getAuthenticationDatabase() {
        return this.authenticationDatabase;
    }

    public void setAuthenticationDatabase(String authenticationDatabase) {
        this.authenticationDatabase = authenticationDatabase;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public char[] getPassword() {
        return this.password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public String getReplicaSetName() {
        return this.replicaSetName;
    }

    public void setReplicaSetName(String replicaSetName) {
        this.replicaSetName = replicaSetName;
    }

    public UuidRepresentation getUuidRepresentation() {
        return this.uuidRepresentation;
    }

    public void setUuidRepresentation(UuidRepresentation uuidRepresentation) {
        this.uuidRepresentation = uuidRepresentation;
    }

    public String getUri() {
        return this.uri;
    }

    public String determineUri() {
        return (this.uri != null) ? this.uri : DEFAULT_URI;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getMongoClientDatabase() {
        if (this.database != null) {
            return this.database;
        }
        return new ConnectionString(determineUri()).getDatabase();
    }

    public Boolean isAutoIndexCreation() {
        return this.autoIndexCreation;
    }

    public void setAutoIndexCreation(Boolean autoIndexCreation) {
        this.autoIndexCreation = autoIndexCreation;
    }

    public Boolean getAutoIndexCreation() {
        return autoIndexCreation;
    }

    public String getEntityPackage() {
        return entityPackage;
    }

    public void setEntityPackage(String entityPackage) {
        this.entityPackage = entityPackage;
    }
}
