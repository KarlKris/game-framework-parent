package com.echo.mongo.index;


import java.util.List;

public interface IndexOperations {


    /**
     * Ensure that an index for the provided {@link IndexDefinition} exists for the collection indicated by the entity
     * class. If not it will be created.
     *
     * @param indexDefinition must not be {@literal null}.
     */
    String ensureIndex(IndexDefinition indexDefinition);

    /**
     * Drops an index from this collection.
     *
     * @param name name of index to hide.
     * @since 4.1
     */
    void alterIndex(String name, IndexOptions options);

    /**
     * Drops an index from this collection.
     *
     * @param name name of index to drop
     */
    void dropIndex(String name);

    /**
     * Drops all indices from this collection.
     */
    void dropAllIndexes();


    /**
     * Returns the index information on the collection.
     *
     * @return index information on the collection
     */
    List<IndexInfo> getIndexInfo();

}
