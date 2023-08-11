package com.echo.mongo.query;

import org.bson.Document;

import static com.echo.common.util.ObjectUtils.nullSafeEquals;
import static com.echo.common.util.ObjectUtils.nullSafeHashCode;

/**
 * 实现从任意JSON查询字符串设置基本查询。
 * @author: li-yuanwen
 */
public class BasicQuery extends Query {

    private final Document queryObject;

    private Document fieldsObject;

    /**
     * Create a new {@link BasicQuery} given a JSON {@code query}.
     *
     * @param query may be {@literal null}.
     */
    public BasicQuery(String query) {
        this(query, null);
    }

    /**
     * Create a new {@link BasicQuery} given a query {@link Document}.
     *
     * @param queryObject must not be {@literal null}.
     */
    public BasicQuery(Document queryObject) {
        this(queryObject, new Document());
    }

    /**
     * Create a new {@link BasicQuery} given a JSON {@code query} and {@code fields}.
     *
     * @param query may be {@literal null}.
     * @param fields may be {@literal null}.
     */
    public BasicQuery(String query, String fields) {

        this(query != null ? Document.parse(query) : new Document(),
                fields != null ? Document.parse(fields) : new Document());
    }

    /**
     * Create a new {@link BasicQuery} given a query {@link Document} and field specification {@link Document}.
     *
     * @param queryObject must not be {@literal null}.
     * @param fieldsObject must not be {@literal null}.
     * @throws IllegalArgumentException when {@code queryObject} or {@code fieldsObject} is {@literal null}.
     */
    public BasicQuery(Document queryObject, Document fieldsObject) {
        this.queryObject = queryObject;
        this.fieldsObject = fieldsObject;
    }

    @Override
    public Query addCriteria(Criteria criteria) {
        this.queryObject.putAll(criteria.getCriteriaObject());
        return this;
    }

    @Override
    public Document getQueryObject() {
        return this.queryObject;
    }

    @Override
    public Document getFieldsObject() {

        Document combinedFieldsObject = new Document();
        combinedFieldsObject.putAll(fieldsObject);
        combinedFieldsObject.putAll(super.getFieldsObject());
        return combinedFieldsObject;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BasicQuery)) {
            return false;
        }
        BasicQuery that = (BasicQuery) o;
        return querySettingsEquals(that) && //
                nullSafeEquals(fieldsObject, that.fieldsObject) && //
                nullSafeEquals(queryObject, that.queryObject);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + nullSafeHashCode(queryObject);
        result = 31 * result + nullSafeHashCode(fieldsObject);

        return result;
    }
}
