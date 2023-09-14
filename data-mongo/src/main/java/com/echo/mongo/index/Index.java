package com.echo.mongo.index;

import com.echo.common.util.StringUtils;
import org.bson.Document;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author: li-yuanwen
 */
public class Index implements IndexDefinition {

    private final Map<String, Direction> fieldSpec = new LinkedHashMap<String, Direction>();
    private String name;
    private boolean sparse = false;
    private boolean background = false;
    private final IndexOptions options = IndexOptions.none();
    private Optional<IndexFilter> filter = Optional.empty();

    public Index() {
    }

    public Index(String key, Direction direction) {
        fieldSpec.put(key, direction);
    }

    public Index on(String key, Direction direction) {
        fieldSpec.put(key, direction);
        return this;
    }

    public Index named(String name) {
        this.name = name;
        return this;
    }

    /**
     * Reject all documents that contain a duplicate value for the indexed field.
     *
     * @return this.
     * @see <a href=
     * "https://docs.mongodb.org/manual/core/index-unique/">https://docs.mongodb.org/manual/core/index-unique/</a>
     */
    public Index unique() {

        this.options.setUnique(IndexOptions.Unique.YES);
        return this;
    }

    /**
     * Skip over any document that is missing the indexed field.
     *
     * @return this.
     * @see <a href=
     * "https://docs.mongodb.org/manual/core/index-sparse/">https://docs.mongodb.org/manual/core/index-sparse/</a>
     */
    public Index sparse() {
        this.sparse = true;
        return this;
    }

    /**
     * Build the index in background (non blocking).
     *
     * @return this.
     * @since 1.5
     */
    public Index background() {

        this.background = true;
        return this;
    }

    /**
     * Hidden indexes are not visible to the query planner and cannot be used to support a query.
     *
     * @return this.
     * @see <a href=
     * "https://www.mongodb.com/docs/manual/core/index-hidden/">https://www.mongodb.com/docs/manual/core/index-hidden/</a>
     * @since 4.1
     */
    public Index hidden() {

        options.setHidden(true);
        return this;
    }

    /**
     * Specifies TTL in seconds.
     *
     * @param value
     * @return this.
     * @since 1.5
     */
    public Index expire(long value) {
        return expire(value, TimeUnit.SECONDS);
    }

    /**
     * Specifies the TTL.
     *
     * @param timeout must not be {@literal null}.
     * @return this.
     * @throws IllegalArgumentException if given {@literal timeout} is {@literal null}.
     * @since 2.2
     */
    public Index expire(Duration timeout) {

        if (timeout == null) {
            throw new IllegalArgumentException("Timeout must not be null");
        }
        return expire(timeout.getSeconds());
    }

    /**
     * Specifies TTL with given {@link TimeUnit}.
     *
     * @param value
     * @param unit  must not be {@literal null}.
     * @return this.
     * @since 1.5
     */
    public Index expire(long value, TimeUnit unit) {

        if (unit == null) {
            throw new IllegalArgumentException("TimeUnit for expiration must not be null");
        }
        options.setExpire(Duration.ofSeconds(unit.toSeconds(value)));
        return this;
    }

    /**
     * Only index the documents in a collection that meet a specified {@link IndexFilter filter expression}.
     *
     * @param filter can be {@literal null}.
     * @return this.
     * @see <a href=
     * "https://docs.mongodb.com/manual/core/index-partial/">https://docs.mongodb.com/manual/core/index-partial/</a>
     * @since 1.10
     */
    public Index partial(IndexFilter filter) {

        this.filter = Optional.ofNullable(filter);
        return this;
    }

    public Document getIndexKeys() {

        Document document = new Document();

        for (Map.Entry<String, Direction> entry : fieldSpec.entrySet()) {
            document.put(entry.getKey(), Direction.ASC.equals(entry.getValue()) ? 1 : -1);
        }

        return document;
    }

    public Document getIndexOptions() {

        Document document = new Document();
        if (StringUtils.hasLength(name)) {
            document.put("name", name);
        }
        if (sparse) {
            document.put("sparse", true);
        }
        if (background) {
            document.put("background", true);
        }
        document.putAll(options.toDocument());

        filter.ifPresent(val -> document.put("partialFilterExpression", val.getFilterObject()));

        return document;
    }

    @Override
    public String toString() {
        return String.format("Index: %s - Options: %s", getIndexKeys(), getIndexOptions());
    }
}
