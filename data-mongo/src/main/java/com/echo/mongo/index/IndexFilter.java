package com.echo.mongo.index;


import org.bson.Document;

/**
 * Use {@link IndexFilter} to create the partial filter expression used when creating
 * <a href="https://docs.mongodb.com/manual/core/index-partial/">Partial Indexes</a>.
 */
public interface IndexFilter {

    /**
     * Get the raw (unmapped) filter expression.
     *
     * @return never {@literal null}.
     */
    Document getFilterObject();

}
