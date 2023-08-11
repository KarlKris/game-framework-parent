package com.echo.mongo.query;

import com.echo.common.util.JsonUtils;
import com.echo.mongo.excetion.InvalidMongoDbApiUsageException;
import com.echo.mongo.util.BsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.Document;

import java.util.*;

import static com.echo.common.util.ObjectUtils.nullSafeEquals;
import static com.echo.common.util.ObjectUtils.nullSafeHashCode;

/**
 * mongodb 查询
 * skip, limit, sort三个放在一起执行的时候，执行的顺序是先 sort, 然后是 skip，最后是显示的 limit。
 * @author: li-yuanwen
 */
public class Query {

    private static final String RESTRICTED_TYPES_KEY = "_$RESTRICTED_TYPES";

    private Set<Class<?>> restrictedTypes = Collections.emptySet();

    /** 查询条件 **/
    private final Map<String, Criteria> criteriaMap = new LinkedHashMap<>();
    /** 跳过数 **/
    private long skip;
    /** 查询数 **/
    private int limit;
    /** 强制使用索引 **/
    private String hint;
    /** field projection **/
    private Fields fieldSpec;

    public Query() {}

    public Query(Criteria criteria) {
        addCriteria(criteria);
    }

    public Query addCriteria(Criteria criteria) {
        String key = criteria.getKey();
        Criteria existing = this.criteriaMap.get(key);
        if (existing == null) {
            this.criteriaMap.put(key, criteria);
        } else {
            throw new InvalidMongoDbApiUsageException(
                    String.format("Due to limitations of the com.mongodb.BasicDocument, you can't add a second '%s' criteria;"
                            + " Query already contains '%s'", key, toJsonSafely(existing.getCriteriaObject())));
        }
        return this;
    }

    /**
     * @return the restrictedTypes
     */
    public Set<Class<?>> getRestrictedTypes() {
        return restrictedTypes;
    }

    /**
     * Restricts the query to only return documents instances that are exactly of the given types.
     *
     * @param type may not be {@literal null}
     * @param additionalTypes may not be {@literal null}
     * @return this.
     */
    public Query restrict(Class<?> type, Class<?>... additionalTypes) {
        if (restrictedTypes == Collections.EMPTY_SET) {
            restrictedTypes = new HashSet<>(1 + additionalTypes.length);
        }
        restrictedTypes.add(type);
        if (additionalTypes.length > 0) {
            restrictedTypes.addAll(Arrays.asList(additionalTypes));
        }
        return this;
    }

    /**
     * @return the query {@link Document}.
     */
    public Document getQueryObject() {

        if (criteriaMap.isEmpty() && restrictedTypes.isEmpty()) {
            return BsonUtils.EMPTY_DOCUMENT;
        }

        if (criteriaMap.size() == 1 && restrictedTypes.isEmpty()) {

            for (Criteria definition : criteriaMap.values()) {
                return definition.getCriteriaObject();
            }
        }

        Document document = new Document();

        for (Criteria definition : criteriaMap.values()) {
            document.putAll(definition.getCriteriaObject());
        }

        if (!restrictedTypes.isEmpty()) {
            document.put(RESTRICTED_TYPES_KEY, getRestrictedTypes());
        }
        return document;
    }

    /**
     * @return the field {@link Document}.
     */
    public Document getFieldsObject() {
        return this.fieldSpec == null ? BsonUtils.EMPTY_DOCUMENT : fieldSpec.getFieldsObject();
    }

    /**
     * Set number of documents to skip before returning results. Use {@literal zero} or a {@literal negative} value to
     * avoid skipping.
     *
     * @param skip number of documents to skip. Use {@literal zero} or a {@literal negative} value to avoid skipping.
     * @return this.
     */
    public Query skip(long skip) {
        this.skip = skip;
        return this;
    }

    /**
     * Limit the number of returned documents to {@code limit}. A {@literal zero} or {@literal negative} value is
     * considered as unlimited.
     *
     * @param limit number of documents to return. Use {@literal zero} or {@literal negative} for unlimited.
     * @return this.
     */
    public Query limit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Configures the query to use the given hint when being executed. The {@code hint} can either be an index name or a
     * json {@link Document} representation.
     *
     * @param hint must not be {@literal null} or empty.
     * @return this.
     * @see Document#parse(String)
     */
    public Query withHint(String hint) {
        this.hint = hint;
        return this;
    }

    public Fields fields() {
        if (this.fieldSpec == null) {
            this.fieldSpec = new Fields();
        }
        return this.fieldSpec;
    }

    public long getSkip() {
        return skip;
    }

    public int getLimit() {
        return limit;
    }

    public String getHint() {
        return hint;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        return querySettingsEquals((Query) obj);
    }

    /**
     * Tests whether the settings of the given {@link Query} are equal to this query.
     *
     * @param that
     * @return
     */
    protected boolean querySettingsEquals(Query that) {
        boolean criteriaEqual = this.criteriaMap.equals(that.criteriaMap);
        boolean fieldsEqual = nullSafeEquals(this.fieldSpec, that.fieldSpec);
        boolean hintEqual = nullSafeEquals(this.hint, that.hint);
        boolean skipEqual = this.skip == that.skip;
        boolean limitEqual = this.limit == that.limit;
        return criteriaEqual && fieldsEqual && hintEqual && skipEqual && limitEqual;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 31 * criteriaMap.hashCode();
        result += 31 * nullSafeHashCode(fieldSpec);
        result += 31 * nullSafeHashCode(hint);
        result += 31 * skip;
        result += 31 * limit;
        return result;
    }

    private static String toJsonSafely(Object object) {
        try {
            return JsonUtils.toJson(object);
        } catch (JsonProcessingException e) {
            return object.toString();
        }
    }

}
