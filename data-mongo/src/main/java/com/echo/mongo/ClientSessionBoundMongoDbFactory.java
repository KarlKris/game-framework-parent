package com.echo.mongo;

import com.echo.mongo.excetion.DataAccessException;
import com.mongodb.ClientSessionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * {@link ClientSession} bound {@link MongoDatabaseFactory} decorating the database
 * @author: li-yuanwen
 */
public class ClientSessionBoundMongoDbFactory implements MongoDatabaseFactory {

    private final ClientSession session;
    private final MongoDatabaseFactory delegate;

    public ClientSessionBoundMongoDbFactory(ClientSession session, MongoDatabaseFactory delegate) {
        this.session = session;
        this.delegate = delegate;
    }

    @Override
    public MongoDatabase getMongoDatabase() throws DataAccessException {
        return proxyMongoDatabase(delegate.getMongoDatabase());
    }

    @Override
    public MongoDatabase getMongoDatabase(String dbName) throws DataAccessException {
        return proxyMongoDatabase(delegate.getMongoDatabase(dbName));
    }

    @Override
    public ClientSession getSession(ClientSessionOptions options) {
        return delegate.getSession(options);
    }

    @Override
    public MongoDatabaseFactory withSession(ClientSession session) {
        return delegate.withSession(session);
    }

    private MongoDatabase proxyMongoDatabase(MongoDatabase database) {
        return createProxyInstance(session, database, MongoDatabase.class);
    }

    private MongoDatabase proxyDatabase(com.mongodb.session.ClientSession session, MongoDatabase database) {
        return createProxyInstance(session, database, MongoDatabase.class);
    }

    private MongoCollection<?> proxyCollection(com.mongodb.session.ClientSession session,
                                               MongoCollection<?> collection) {
        return createProxyInstance(session, collection, MongoCollection.class);
    }

    private <T> T createProxyInstance(com.mongodb.session.ClientSession session, T target, Class<T> targetType) {
        try {
            // todo 后续转成bytecode
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(targetType);
            return targetType.cast(factory.create(new Class[0], new Object[0]
                    , new SessionAwareMethodHandler<>(session, target
                            , ClientSession.class, MongoDatabase.class, this::proxyDatabase
                            , MongoCollection.class, this::proxyCollection)));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
