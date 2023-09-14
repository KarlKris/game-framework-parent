package com.echo.mongo.index;

import org.bson.Document;

/**
 * Index definition to span multiple keys.
 *
 * @author: li-yuanwen
 */
public class CompoundIndexDefinition extends Index {

    private final Document keys;

    /**
     * Creates a new {@link CompoundIndexDefinition} for the given keys.
     *
     * @param keys must not be {@literal null}.
     */
    public CompoundIndexDefinition(Document keys) {

        if (keys == null) {
            throw new IllegalArgumentException("Keys must not be null");
        }

        this.keys = keys;
    }

    @Override
    public Document getIndexKeys() {

        Document document = new Document();
        document.putAll(this.keys);
        document.putAll(super.getIndexKeys());
        return document;
    }
}
