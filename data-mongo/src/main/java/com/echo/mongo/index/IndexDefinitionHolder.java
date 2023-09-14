package com.echo.mongo.index;

import org.bson.Document;

/**
 * @author: li-yuanwen
 */
public class IndexDefinitionHolder implements IndexDefinition {

    private final IndexDefinition indexDefinition;
    private final String collection;

    public IndexDefinitionHolder(IndexDefinition indexDefinition, String collection) {
        this.indexDefinition = indexDefinition;
        this.collection = collection;
    }

    public String getCollection() {
        return collection;
    }

    @Override
    public Document getIndexKeys() {
        return indexDefinition.getIndexKeys();
    }

    @Override
    public Document getIndexOptions() {
        return indexDefinition.getIndexOptions();
    }
}
