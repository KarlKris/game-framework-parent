package com.echo.mongo.core;

/**
 * mongodb 操作集合
 */
public interface MongoOperations extends FindOperation, InsertOperation, RemoveOperation, UpdateOperation {


    /**
     * 根据实体类型映射mongodb中的document
     * @param entityClass 实体类型
     * @return mongodb中对应的document
     */
    String getCollectionName(Class<?> entityClass);



}
