package com.echo.mongo.core;

import com.mongodb.bulk.BulkWriteResult;

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


    /**
     * Trigger insert execution by calling one of the terminating methods.
     *
     * @author Christoph Strobl
     * @since 2.0
     */
    interface TerminatingInsert<T> extends TerminatingBulkInsert<T> {

        /**
         * Insert exactly one object.
         *
         * @param object must not be {@literal null}.
         * @return the inserted object.
         * @throws IllegalArgumentException if object is {@literal null}.
         */
        T one(T object);

        /**
         * Insert a collection of objects.
         *
         * @param objects must not be {@literal null}.
         * @return the inserted objects.
         * @throws IllegalArgumentException if objects is {@literal null}.
         */
        Collection<? extends T> all(Collection<? extends T> objects);
    }

    /**
     * Trigger bulk insert execution by calling one of the terminating methods.
     *
     * @author Christoph Strobl
     * @since 2.0
     */
    interface TerminatingBulkInsert<T> {

        /**
         * Bulk write collection of objects.
         *
         * @param objects must not be {@literal null}.
         * @return resulting {@link BulkWriteResult}.
         * @throws IllegalArgumentException if objects is {@literal null}.
         */
        BulkWriteResult bulk(Collection<? extends T> objects);
    }

    /**
     * Collection override (optional).
     *
     * @author Christoph Strobl
     * @since 2.0
     */
    interface InsertWithCollection<T> {

        /**
         * Explicitly set the name of the collection. <br />
         * Skip this step to use the default collection derived from the domain type.
         *
         * @param collection must not be {@literal null} nor {@literal empty}.
         * @return new instance of {@link InsertWithBulkMode}.
         * @throws IllegalArgumentException if collection is {@literal null}.
         */
        InsertWithBulkMode<T> inCollection(String collection);
    }

    /**
     * @author Christoph Strobl
     * @since 2.0
     */
    interface InsertWithBulkMode<T> extends TerminatingInsert<T> {

        /**
         * Define the {@link com.echo.mongo.core.BulkOperations.BulkMode} to use for bulk insert operation.
         *
         * @param bulkMode must not be {@literal null}.
         * @return new instance of {@link TerminatingBulkInsert}.
         * @throws IllegalArgumentException if bulkMode is {@literal null}.
         */
        TerminatingBulkInsert<T> withBulkMode(BulkOperations.BulkMode bulkMode);
    }

    /**
     * @author Christoph Strobl
     * @since 2.0
     */
    interface ExecutableInsert<T> extends TerminatingInsert<T>, InsertWithCollection<T>, InsertWithBulkMode<T> {}

}
