package com.echo.mongo.index;

import org.bson.Document;

public interface IndexDefinition {

    /**
     * Get the {@link Document} containing properties covered by the index.
     *
     * @return never {@literal null}.
     */
    Document getIndexKeys();

    /**
     * Get the index properties such as {@literal unique},...
     *
     * @return never {@literal null}.
     */
    Document getIndexOptions();

}
