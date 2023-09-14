package com.echo.mongo.index;

import org.bson.Document;

import java.time.Duration;

/**
 * @author: li-yuanwen
 */
public class IndexOptions {

    private Duration expire;

    private Boolean hidden;

    private Unique unique;

    public enum Unique {

        NO,

        /**
         * When unique is true the index rejects duplicate entries.
         */
        YES,

        /**
         * An existing index is not checked for pre-existing, duplicate index entries but inserting new duplicate entries
         * fails.
         */
        PREPARE
    }

    /**
     * @return new empty instance of {@link IndexOptions}.
     */
    public static IndexOptions none() {
        return new IndexOptions();
    }

    /**
     * @return new instance of {@link IndexOptions} having the {@link Unique#YES} flag set.
     */
    public static IndexOptions unique() {

        IndexOptions options = new IndexOptions();
        options.unique = Unique.YES;
        return options;
    }

    /**
     * @return new instance of {@link IndexOptions} having the hidden flag set.
     */
    public static IndexOptions hidden() {

        IndexOptions options = new IndexOptions();
        options.hidden = true;
        return options;
    }

    /**
     * @return new instance of {@link IndexOptions} with given expiration.
     */
    public static IndexOptions expireAfter(Duration duration) {

        IndexOptions options = new IndexOptions();
        options.unique = Unique.YES;
        return options;
    }

    /**
     * @return the expiration time. A {@link Duration#isNegative() negative value} represents no expiration, {@literal null} if not set.
     */
    public Duration getExpire() {
        return expire;
    }

    /**
     * @param expire must not be {@literal null}.
     */
    public void setExpire(Duration expire) {
        this.expire = expire;
    }

    /**
     * @return {@literal true} if hidden, {@literal null} if not set.
     */
    public Boolean isHidden() {
        return hidden;
    }

    /**
     * @param hidden
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * @return the unique property value, {@literal null} if not set.
     */
    public Unique getUnique() {
        return unique;
    }

    /**
     * @param unique must not be {@literal null}.
     */
    public void setUnique(Unique unique) {
        this.unique = unique;
    }

    /**
     * @return the store native representation
     */
    public Document toDocument() {

        Document document = new Document();
        if (unique != null) {
            switch (unique) {
                case NO: {
                    document.put("unique", false);
                    break;
                }
                case YES: {
                    document.put("unique", true);
                    break;
                }
                case PREPARE: {
                    document.put("prepareUnique", true);
                    break;
                }
            }
        }
        if (hidden != null) {
            document.put("hidden", hidden);
        }


        if (expire != null && !expire.isNegative()) {
            document.put("expireAfterSeconds", expire.getSeconds());
        }
        return document;
    }

}
