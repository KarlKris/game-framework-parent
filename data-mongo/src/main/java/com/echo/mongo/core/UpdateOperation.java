package com.echo.mongo.core;

import com.echo.mongo.query.Query;
import com.echo.mongo.query.UpdateDefinition;
import com.mongodb.client.result.UpdateResult;

/**
 * mongodb 查询操作
 */
public interface UpdateOperation {

    /**
     * Performs an upsert. If no document is found that matches the query, a new document is created and inserted by
     * combining the query document and the update document. <br />
     *
     * @param query       the query document that specifies the criteria used to select a record to be upserted. Must not be
     *                    {@literal null}.
     * @param update      the {@link UpdateDefinition} that contains the updated object or {@code $} operators to manipulate
     *                    the existing object. Must not be {@literal null}.
     * @param entityClass class that determines the collection to use. Must not be {@literal null}.
     * @return the {@link UpdateResult} which lets you access the results of the previous write.
     * @throws com.echo.mongo.excetion.MappingException if the target collection name cannot be
     *                                                  {@link MongoOperations#getCollectionName(Class) derived} from the given type.
     * @see com.echo.mongo.query.Update
     */
    UpdateResult upsert(Query query, UpdateDefinition update, Class<?> entityClass);

    /**
     * Performs an upsert. If no document is found that matches the query, a new document is created and inserted by
     * combining the query document and the update document. <br />
     * <strong>NOTE:</strong> Any additional support for field mapping, versions, etc. is not available due to the lack of
     * domain type information. Use {@link #upsert(Query, UpdateDefinition, Class, String)} to get full type specific
     * support. <br />
     *
     * @param query          the query document that specifies the criteria used to select a record to be upserted. Must not be
     *                       {@literal null}.
     * @param update         the {@link UpdateDefinition} that contains the updated object or {@code $} operators to manipulate
     *                       the existing object. Must not be {@literal null}.
     * @param collectionName name of the collection to update the object in.
     * @return the {@link UpdateResult} which lets you access the results of the previous write.
     * @see com.echo.mongo.query.Update
     */
    UpdateResult upsert(Query query, UpdateDefinition update, String collectionName);

    /**
     * Performs an upsert. If no document is found that matches the query, a new document is created and inserted by
     * combining the query document and the update document.
     *
     * @param query          the query document that specifies the criteria used to select a record to be upserted. Must not be
     *                       {@literal null}.
     * @param update         the {@link UpdateDefinition} that contains the updated object or {@code $} operators to manipulate
     *                       the existing object. Must not be {@literal null}.
     * @param entityClass    class of the pojo to be operated on. Must not be {@literal null}.
     * @param collectionName name of the collection to update the object in. Must not be {@literal null}.
     * @return the {@link UpdateResult} which lets you access the results of the previous write.
     * @see com.echo.mongo.query.Update
     */
    UpdateResult upsert(Query query, UpdateDefinition update, Class<?> entityClass, String collectionName);


    /**
     * Updates the first object that is found in the collection of the entity class that matches the query document with
     * the provided update document.
     *
     * @param query       the query document that specifies the criteria used to select a record to be updated. Must not be
     *                    {@literal null}.
     * @param update      the {@link UpdateDefinition} that contains the updated object or {@code $} operators to manipulate
     *                    the existing. Must not be {@literal null}.
     * @param entityClass class that determines the collection to use.
     * @return the {@link UpdateResult} which lets you access the results of the previous write.
     * @throws com.echo.mongo.excetion.MappingException if the target collection name cannot be
     *                                                  {@link MongoOperations#getCollectionName(Class) derived} from the given type.
     * @see com.echo.mongo.query.Update
     * @since 3.0
     */
    UpdateResult updateFirst(Query query, UpdateDefinition update, Class<?> entityClass);

    /**
     * Updates the first object that is found in the specified collection that matches the query document criteria with
     * the provided updated document. <br />
     * <strong>NOTE:</strong> Any additional support for field mapping, versions, etc. is not available due to the lack of
     * domain type information. Use {@link #updateFirst(Query, UpdateDefinition, Class, String)} to get full type specific
     * support. <br />
     *
     * @param query          the query document that specifies the criteria used to select a record to be updated. Must not be
     *                       {@literal null}.
     * @param update         the {@link UpdateDefinition} that contains the updated object or {@code $} operators to manipulate
     *                       the existing. Must not be {@literal null}.
     * @param collectionName name of the collection to update the object in. Must not be {@literal null}.
     * @return the {@link UpdateResult} which lets you access the results of the previous write.
     * @see com.echo.mongo.query.Update
     * @since 3.0
     */
    UpdateResult updateFirst(Query query, UpdateDefinition update, String collectionName);

    /**
     * Updates the first object that is found in the specified collection that matches the query document criteria with
     * the provided updated document. <br />
     *
     * @param query          the query document that specifies the criteria used to select a record to be updated. Must not be
     *                       {@literal null}.
     * @param update         the {@link UpdateDefinition} that contains the updated object or {@code $} operators to manipulate
     *                       the existing. Must not be {@literal null}.
     * @param entityClass    class of the pojo to be operated on. Must not be {@literal null}.
     * @param collectionName name of the collection to update the object in. Must not be {@literal null}.
     * @return the {@link UpdateResult} which lets you access the results of the previous write.
     * @see com.echo.mongo.query.Update
     */
    UpdateResult updateFirst(Query query, UpdateDefinition update, Class<?> entityClass, String collectionName);

    /**
     * Updates all objects that are found in the collection for the entity class that matches the query document criteria
     * with the provided updated document.
     *
     * @param query       the query document that specifies the criteria used to select a record to be updated. Must not be
     *                    {@literal null}.
     * @param update      the {@link UpdateDefinition} that contains the updated object or {@code $} operators to manipulate
     *                    the existing. Must not be {@literal null}.
     * @param entityClass class of the pojo to be operated on. Must not be {@literal null}.
     * @return the {@link UpdateResult} which lets you access the results of the previous write.
     * @throws com.echo.mongo.excetion.MappingException if the target collection name cannot be
     *                                                  {@link MongoOperations#getCollectionName(Class) derived} from the given type.
     * @see com.echo.mongo.query.Update
     */
    UpdateResult updateMulti(Query query, UpdateDefinition update, Class<?> entityClass);

    /**
     * Updates all objects that are found in the specified collection that matches the query document criteria with the
     * provided updated document. <br />
     * <strong>NOTE:</strong> Any additional support for field mapping, versions, etc. is not available due to the lack of
     * domain type information. Use {@link #updateMulti(Query, UpdateDefinition, Class, String)} to get full type specific
     * support.
     *
     * @param query          the query document that specifies the criteria used to select a record to be updated. Must not be
     *                       {@literal null}.
     * @param update         the {@link UpdateDefinition} that contains the updated object or {@code $} operators to manipulate
     *                       the existing. Must not be {@literal null}.
     * @param collectionName name of the collection to update the object in. Must not be {@literal null}.
     * @return the {@link UpdateResult} which lets you access the results of the previous write.
     * @see com.echo.mongo.query.Update
     */
    UpdateResult updateMulti(Query query, UpdateDefinition update, String collectionName);

    /**
     * Updates all objects that are found in the collection for the entity class that matches the query document criteria
     * with the provided updated document.
     *
     * @param query          the query document that specifies the criteria used to select a record to be updated. Must not be
     *                       {@literal null}.
     * @param update         the {@link UpdateDefinition} that contains the updated object or {@code $} operators to manipulate
     *                       the existing. Must not be {@literal null}.
     * @param entityClass    class of the pojo to be operated on. Must not be {@literal null}.
     * @param collectionName name of the collection to update the object in. Must not be {@literal null}.
     * @return the {@link UpdateResult} which lets you access the results of the previous write.
     * @see com.echo.mongo.query.Update
     */
    UpdateResult updateMulti(Query query, UpdateDefinition update, Class<?> entityClass, String collectionName);

}

