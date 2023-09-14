package com.echo.autoconfigure.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import java.util.Collections;

/**
 * @author: li-yuanwen
 */
public class MongoPropertiesClientSettingsBuilderCustomizer implements MongoClientSettingsBuilderCustomizer {

    private final MongoProperties properties;

    public MongoPropertiesClientSettingsBuilderCustomizer(MongoProperties properties) {
        this.properties = properties;
    }

    @Override
    public void customize(MongoClientSettings.Builder settingsBuilder) {
        validateConfiguration();
        applyUuidRepresentation(settingsBuilder);
        applyHostAndPort(settingsBuilder);
        applyCredentials(settingsBuilder);
        applyReplicaSet(settingsBuilder);
    }

    private void validateConfiguration() {
        if (hasCustomAddress() || hasCustomCredentials() || hasReplicaSet()) {
            if (this.properties.getUri() != null) {
                throw new IllegalStateException("Invalid mongo configuration, either uri or host/port/credentials/replicaSet must be specified");
            }
        }
    }

    private void applyUuidRepresentation(MongoClientSettings.Builder settingsBuilder) {
        settingsBuilder.uuidRepresentation(this.properties.getUuidRepresentation());
    }

    private void applyHostAndPort(MongoClientSettings.Builder settings) {
        if (hasCustomAddress()) {
            String host = getOrDefault(this.properties.getHost(), "localhost");
            int port = getOrDefault(this.properties.getPort(), MongoProperties.DEFAULT_PORT);
            ServerAddress serverAddress = new ServerAddress(host, port);
            settings.applyToClusterSettings((cluster) -> cluster.hosts(Collections.singletonList(serverAddress)));
            return;
        }

        settings.applyConnectionString(new ConnectionString(this.properties.determineUri()));
    }

    private void applyCredentials(MongoClientSettings.Builder builder) {
        if (hasCustomCredentials()) {
            String database = (this.properties.getAuthenticationDatabase() != null)
                    ? this.properties.getAuthenticationDatabase() : this.properties.getMongoClientDatabase();
            builder.credential((MongoCredential.createCredential(this.properties.getUsername(), database,
                    this.properties.getPassword())));
        }
    }

    private void applyReplicaSet(MongoClientSettings.Builder builder) {
        if (hasReplicaSet()) {
            builder.applyToClusterSettings(
                    (cluster) -> cluster.requiredReplicaSetName(this.properties.getReplicaSetName()));
        }
    }

    private <V> V getOrDefault(V value, V defaultValue) {
        return (value != null) ? value : defaultValue;
    }

    private boolean hasCustomCredentials() {
        return this.properties.getUsername() != null && this.properties.getPassword() != null;
    }

    private boolean hasCustomAddress() {
        return this.properties.getHost() != null || this.properties.getPort() != null;
    }

    private boolean hasReplicaSet() {
        return this.properties.getReplicaSetName() != null;
    }
}
