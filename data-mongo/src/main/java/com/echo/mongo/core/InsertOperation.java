package com.echo.mongo.core;

import java.util.Collection;

/**
 * mongodb 增加操作
 */
public interface InsertOperation {

    /**
     * Insert the object into the collection for the entity type of the object to save. <br />
     * The object is converted to the MongoDB native representation using an instance of {@see MongoConverter}. <br />
     * If your object has an {@literal Id} property which holds a {@literal null} value, it will be set with the generated
     * Id from MongoDB. If your Id property is a String then MongoDB ObjectId will be used to populate that string.
     * Otherwise, the conversion from ObjectId to your property type will be handled by Spring's BeanWrapper class that
     * leverages Type Conversion API. See
     * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#validation" > Spring's
     * Type Conversion"</a> for more details. <br />
     * Insert is used to initially store the object into the database. To update an existing object use the save method.
     * <br />
     * The {@code objectToSave} must not be collection-like.
     *
     * @param objectToSave the object to store in the collection. Must not be {@literal null}.
     * @return the inserted object.
     * @throws IllegalArgumentException in case the {@code objectToSave} is collection-like.
     * @throws com.echo.mongo.excetion.MappingException if the target collection name cannot be
     *           {@link MongoOperations#getCollectionName(Class) derived} from the given object type.
     */
    <T> T insert(T objectToSave);

    <T> T insert(T objectToSave, String collectionName);


    /**
     * Insert a Collection of objects into a collection in a single batch write to the database.
     *
     * @param batchToSave the batch of objects to save. Must not be {@literal null}.
     * @param entityClass class that determines the collection to use. Must not be {@literal null}.
     * @return the inserted objects that.
     * @throws com.echo.mongo.excetion.MappingException if the target collection name cannot be
     *                                                  {@link MongoOperations#getCollectionName(Class) derived} from the given type.
     */
    <T> Collection<T> insert(Collection<? extends T> batchToSave, Class<?> entityClass);

    /**
     * Insert a batch of objects into the specified collection in a single batch write to the database.
     *
     * @param batchToSave    the list of objects to save. Must not be {@literal null}.
     * @param collectionName name of the collection to store the object in. Must not be {@literal null}.
     * @return the inserted objects that.
     */
    <T> Collection<T> insert(Collection<? extends T> batchToSave, String collectionName);

    /**
     * Insert a mixed Collection of objects into a database collection determining the collection name to use based on the
     * class.
     *
     * @param objectsToSave the list of objects to save. Must not be {@literal null}.
     * @return the inserted objects.
     * @throws com.echo.mongo.excetion.MappingException if the target collection name cannot be
     *                                                  {@link MongoOperations#getCollectionName(Class) derived} for the given objects.
     */
    <T> Collection<T> insertAll(Collection<? extends T> objectsToSave);

}
