package com.echo.mongo.core;

import com.echo.mongo.query.Query;

import java.util.List;

/**
 * mongodb 查询操作
 */
public interface FindOperation {


    <T> List<T> find(Query query, Class<T> entityClass);


    <T> List<T> find(Query query, Class<T> entityClass, String collectionName);


    <T> T findOne(Query query, Class<T> entityClass);


    <T> T findOne(Query query, Class<T> entityClass, String collectionName);

    <T> T findById(Object id, Class<T> entityClass);

    <T> T findById(Object id, Class<T> entityClass, String collectionName);

}
