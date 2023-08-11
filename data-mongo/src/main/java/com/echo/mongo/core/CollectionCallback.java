package com.echo.mongo.core;

import com.echo.mongo.excetion.DataAccessException;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

/**
 * Callback interface for executing actions against a {@link MongoCollection}.
 *
 * @author Mark Pollak
 * @author Grame Rocher
 * @author Oliver Gierke
 * @author John Brisbin
 * @author Christoph Strobl
 * @since 1.0
 */
public interface CollectionCallback<T> {

    /**
     * @param collection never {@literal null}.
     * @return can be {@literal null}.
     * @throws MongoException
     * @throws DataAccessException
     */
    T doInCollection(MongoCollection<Document> collection) throws MongoException, DataAccessException;

}