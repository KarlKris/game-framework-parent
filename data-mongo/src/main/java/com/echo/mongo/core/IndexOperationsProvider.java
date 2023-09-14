package com.echo.mongo.core;

import com.echo.mongo.index.IndexOperations;

/**
 * Provider interface to obtain {@link IndexOperations} by MongoDB collection name.
 */
public interface IndexOperationsProvider {

    /**
     * Returns the operations that can be performed on indexes.
     *
     * @param collectionName name of the MongoDB collection, must not be {@literal null}.
     * @return index operations on the named collection
     */
    default IndexOperations indexOps(String collectionName) {
        return indexOps(collectionName, null);
    }

    /**
     * Returns the operations that can be performed on indexes.
     *
     * @param collectionName name of the MongoDB collection, must not be {@literal null}.
     * @param type           the type used for field mapping. Can be {@literal null}.
     * @return index operations on the named collection
     * @since 3.2
     */
    IndexOperations indexOps(String collectionName, Class<?> type);

}
