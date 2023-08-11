package com.echo.mongo;

import com.echo.common.util.StringUtils;
import com.echo.mongo.excetion.DataAccessException;
import com.echo.mongo.excetion.InvalidMongoDbApiUsageException;
import com.mongodb.ClientSessionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * MongoDatabase 工厂
 * @author: li-yuanwen
 */
public class SimpleMongoDatabaseFactory implements MongoDatabaseFactory {


    private final MongoClient mongoClient;
    private final String databaseName;

    private WriteConcern writeConcern;

    public SimpleMongoDatabaseFactory(MongoClient mongoClient, String databaseName) {
        this.mongoClient = mongoClient;
        this.databaseName = databaseName;
    }

    public void setWriteConcern(WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * @return the database name.
     */
    protected String getDefaultDatabaseName() {
        return databaseName;
    }

    protected MongoDatabase doGetMongoDatabase(String dbName) {
        return getMongoClient().getDatabase(dbName);
    }

    @Override
    public MongoDatabase getMongoDatabase() throws DataAccessException {
        return getMongoDatabase(getDefaultDatabaseName());
    }

    @Override
    public MongoDatabase getMongoDatabase(String dbName) throws DataAccessException {
        if (!StringUtils.hasLength(dbName)) {
            throw new InvalidMongoDbApiUsageException("Database name must not be empty");
        }

        MongoDatabase db = doGetMongoDatabase(dbName);

        if (writeConcern == null) {
            return db;
        }

        return db.withWriteConcern(writeConcern);
    }

    @Override
    public ClientSession getSession(ClientSessionOptions options) {
        return getMongoClient().startSession(options);
    }

    @Override
    public MongoDatabaseFactory withSession(ClientSession session) {
        return  new ClientSessionBoundMongoDbFactory(session, this);
    }
}
