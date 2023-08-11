package com.echo.mongo;

import com.echo.mongo.excetion.DataAccessException;
import com.mongodb.ClientSessionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;

public interface MongoDatabaseFactory extends CodecRegistryProvider {

    /**
     * Obtain a {@link MongoDatabase} from the underlying factory.
     *
     * @return never {@literal null}.
     * @throws DataAccessException
     */
    MongoDatabase getMongoDatabase() throws DataAccessException;

    /**
     * Obtain a {@link MongoDatabase} instance to access the database with the given name.
     *
     * @param dbName must not be {@literal null}.
     * @return never {@literal null}.
     * @throws DataAccessException
     */
    MongoDatabase getMongoDatabase(String dbName) throws DataAccessException;


    /**
     * Obtain a {@link ClientSession} for given ClientSessionOptions.
     *
     * @param options must not be {@literal null}.
     * @return never {@literal null}.
     * @since 2.1
     */
    ClientSession getSession(ClientSessionOptions options);

    /**
     * Obtain a {@link ClientSession} bound instance of {@link MongoDatabaseFactory} returning {@link MongoDatabase}
     * instances that are aware and bound to a new session with given {@link ClientSessionOptions options}.
     *
     * @param options must not be {@literal null}.
     * @return never {@literal null}.
     * @since 2.1
     */
    default MongoDatabaseFactory withSession(ClientSessionOptions options) {
        return withSession(getSession(options));
    }

    /**
     * Obtain a {@link ClientSession} bound instance of {@link MongoDatabaseFactory} returning {@link MongoDatabase}
     * instances that are aware and bound to the given session.
     *
     * @param session must not be {@literal null}.
     * @return never {@literal null}.
     * @since 2.1
     */
    MongoDatabaseFactory withSession(ClientSession session);


    /**
     * Get the underlying {@link CodecRegistry} used by the MongoDB Java driver.
     *
     * @return never {@literal null}.
     */
    @Override
    default CodecRegistry getCodecRegistry() {
        return getMongoDatabase().getCodecRegistry();
    }
}
