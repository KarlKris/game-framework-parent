package com.echo.mongo.index;

import com.echo.mongo.query.Criteria;
import org.bson.Document;

/**
 * {@link IndexFilter} implementation for usage with plain {@link Document} as well as {@link Criteria} filter
 * expressions.
 *
 * @author Christoph Strobl
 * @since 1.10
 */
public class PartialIndexFilter implements IndexFilter {

    private final Object filterExpression;

    private PartialIndexFilter(Object filterExpression) {

        if (filterExpression == null) {
            throw new IllegalArgumentException("FilterExpression must not be null");
        }

        this.filterExpression = filterExpression;
    }

    /**
     * Create new {@link PartialIndexFilter} for given {@link Document filter expression}.
     *
     * @param where must not be {@literal null}.
     * @return
     */
    public static PartialIndexFilter of(Document where) {
        return new PartialIndexFilter(where);
    }

    /**
     * Create new {@link PartialIndexFilter} for given {@link Criteria filter expression}.
     *
     * @param where must not be {@literal null}.
     * @return
     */
    public static PartialIndexFilter of(Criteria where) {
        return new PartialIndexFilter(where);
    }

    public Document getFilterObject() {

        if (filterExpression instanceof Document) {
            return (Document) filterExpression;
        }

        if (filterExpression instanceof Criteria) {
            return ((Criteria) filterExpression).getCriteriaObject();
        }

        throw new IllegalArgumentException(
                String.format("Unknown type %s used as filter expression", filterExpression.getClass()));
    }
}
