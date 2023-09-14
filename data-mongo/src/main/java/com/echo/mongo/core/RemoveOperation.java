package com.echo.mongo.core;

import com.echo.mongo.query.Query;
import com.mongodb.client.result.DeleteResult;

/**
 * mongodb 删除操作
 */
public interface RemoveOperation {

    /**
     * Remove the given object from the collection by {@literal id}
     * Use {@link DeleteResult#getDeletedCount()} for insight whether an {@link DeleteResult#wasAcknowledged()
     * acknowledged} remove operation was successful or not.
     *
     * @param object must not be {@literal null}.
     * @return the {@link DeleteResult} which lets you access the results of the previous delete.
     * @throws com.echo.mongo.excetion.MappingException if the target collection name cannot be
     *                                                  {@link MongoOperations#getCollectionName(Class) derived} from the given object type.
     */
    DeleteResult remove(Object object);

    /**
     * Removes the given object from the given collection by {@literal id}
     * Use {@link DeleteResult#getDeletedCount()} for insight whether an {@link DeleteResult#wasAcknowledged()
     * acknowledged} remove operation was successful or not.
     *
     * @param object         must not be {@literal null}.
     * @param collectionName name of the collection where the objects will removed, must not be {@literal null} or empty.
     * @return the {@link DeleteResult} which lets you access the results of the previous delete.
     */
    DeleteResult remove(Object object, String collectionName);

    /**
     * Remove all documents that match the provided query document criteria from the collection used to store the
     * entityClass. The Class parameter is also used to help convert the Id of the object if it is present in the query.
     *
     * @param query       the query document that specifies the criteria used to remove a record.
     * @param entityClass class that determines the collection to use.
     * @return the {@link DeleteResult} which lets you access the results of the previous delete.
     * @throws IllegalArgumentException                 when {@literal query} or {@literal entityClass} is {@literal null}.
     * @throws com.echo.mongo.excetion.MappingException if the target collection name cannot be
     *                                                  {@link MongoOperations#getCollectionName(Class) derived} from the given type.
     */
    DeleteResult remove(Query query, Class<?> entityClass);

    /**
     * Remove all documents that match the provided query document criteria from the collection used to store the
     * entityClass. The Class parameter is also used to help convert the Id of the object if it is present in the query.
     *
     * @param query          the query document that specifies the criteria used to remove a record.
     * @param entityClass    class of the pojo to be operated on. Can be {@literal null}.
     * @param collectionName name of the collection where the objects will removed, must not be {@literal null} or empty.
     * @return the {@link DeleteResult} which lets you access the results of the previous delete.
     * @throws IllegalArgumentException when {@literal query}, {@literal entityClass} or {@literal collectionName} is
     *                                  {@literal null}.
     */
    DeleteResult remove(Query query, Class<?> entityClass, String collectionName);

    /**
     * Remove all documents from the specified collection that match the provided query document criteria. There is no
     * conversion/mapping done for any criteria using the id field. <br />
     * <strong>NOTE:</strong> Any additional support for field mapping is not available due to the lack of domain type
     * information. Use {@link #remove(Query, Class, String)} to get full type specific support.
     *
     * @param query          the query document that specifies the criteria used to remove a record.
     * @param collectionName name of the collection where the objects will removed, must not be {@literal null} or empty.
     * @return the {@link DeleteResult} which lets you access the results of the previous delete.
     * @throws IllegalArgumentException when {@literal query} or {@literal collectionName} is {@literal null}.
     */
    DeleteResult remove(Query query, String collectionName);

}
