package com.echo.mongo.core;

import com.mongodb.client.MongoCollection;

/**
 * mongodb 操作集合
 */
public interface MongoOperations extends FindOperation, InsertOperation, RemoveOperation, UpdateOperation {


    /**
     * 根据实体类型映射mongodb中的document
     *
     * @param entityClass 实体类型
     * @return mongodb中对应的document
     */
    String getCollectionName(Class<?> entityClass);

    /**
     * Executes a {@link DbCallback} translating any exceptions as necessary. <br />
     * Allows for returning a result object, that is a domain object or a collection of domain objects.
     *
     * @param action callback object that specifies the MongoDB actions to perform on the passed in DB instance. Must not
     *               be {@literal null}.
     * @param <T>    return type.
     * @return a result object returned by the action or {@literal null}.
     */
    <T> T execute(DbCallback<T> action);

    /**
     * Executes the given {@link CollectionCallback} on the entity collection of the specified class. <br />
     * Allows for returning a result object, that is a domain object or a collection of domain objects.
     *
     * @param entityClass class that determines the collection to use. Must not be {@literal null}.
     * @param action      callback object that specifies the MongoDB action. Must not be {@literal null}.
     * @param <T>         return type.
     * @return a result object returned by the action or {@literal null}.
     */
    <T> T execute(Class<?> entityClass, CollectionCallback<T> action);


    /**
     * Executes the given {@link CollectionCallback} on the collection of the given name. <br />
     * Allows for returning a result object, that is a domain object or a collection of domain objects.
     *
     * @param collectionName the name of the collection that specifies which {@link MongoCollection} instance will be
     *                       passed into. Must not be {@literal null} or empty.
     * @param action         callback object that specifies the MongoDB action the callback action. Must not be {@literal null}.
     * @param <T>            return type.
     * @return a result object returned by the action or {@literal null}.
     */
    <T> T execute(String collectionName, CollectionCallback<T> action);


    /**
     * Save the object to the collection for the entity type of the object to save. This will perform an insert if the
     * object is not already present, that is an 'upsert'. <br />
     * The object is converted to the MongoDB native representation using an instance of {@see MongoConverter}. Unless
     * configured otherwise, an instance of {@link com.echo.mongo.convert.GenericMongoConverter} will be used. <br />
     * If your object has an "Id' property, it will be set with the generated Id from MongoDB. If your Id property is a
     * String then MongoDB ObjectId will be used to populate that string. Otherwise, the conversion from ObjectId to your
     * property type will be handled by Spring's BeanWrapper class that leverages Type Conversion API. See
     * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#validation" > Spring's
     * Type Conversion"</a> for more details. <br />
     * The {@code objectToSave} must not be collection-like.
     *
     * @param objectToSave the object to store in the collection. Must not be {@literal null}.
     * @return the saved object.
     * @throws IllegalArgumentException                 in case the {@code objectToSave} is collection-like.
     * @throws com.echo.mongo.excetion.MappingException if the target collection name cannot be
     *                                                  {@link MongoOperations#getCollectionName(Class) derived} from the given object type.
     */
    <T> T save(T objectToSave);

    /**
     * Save the object to the specified collection. This will perform an insert if the object is not already present, that
     * is an 'upsert'. <br />
     * The object is converted to the MongoDB native representation using an instance of {@see MongoConverter}. Unless
     * configured otherwise, an instance of {@link com.echo.mongo.convert.GenericMongoConverter} will be used. <br />
     * If your object has an "Id' property, it will be set with the generated Id from MongoDB. If your Id property is a
     * String then MongoDB ObjectId will be used to populate that string. Otherwise, the conversion from ObjectId to your
     * property type will be handled by Spring's BeanWrapper class that leverages Type Conversion API. See
     * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#validation">Spring's Type
     * Conversion</a> for more details. <br />
     * The {@code objectToSave} must not be collection-like.
     *
     * @param objectToSave   the object to store in the collection. Must not be {@literal null}.
     * @param collectionName name of the collection to store the object in. Must not be {@literal null}.
     * @return the saved object.
     * @throws IllegalArgumentException in case the {@code objectToSave} is collection-like.
     */
    <T> T save(T objectToSave, String collectionName);


}
