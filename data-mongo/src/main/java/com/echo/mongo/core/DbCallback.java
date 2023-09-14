package com.echo.mongo.core;

import com.echo.mongo.excetion.DataAccessException;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;

public interface DbCallback<T> {

    /**
     * @param db must not be {@literal null}.
     * @return can be {@literal null}.
     * @throws MongoException
     * @throws DataAccessException
     */
    T doInDB(MongoDatabase db) throws MongoException, DataAccessException;
}
