package com.echo.mongo.index;

import com.echo.common.util.NumberUtils;
import com.echo.common.util.ObjectUtils;
import com.echo.mongo.util.SerializationUtils;
import org.bson.Document;

import java.time.Duration;
import java.util.*;

import static com.echo.mongo.index.Direction.ASC;
import static com.echo.mongo.index.Direction.DESC;

/**
 * @author: li-yuanwen
 */
public class IndexInfo {

    private static final Double ONE = Double.valueOf(1);
    private static final Double MINUS_ONE = Double.valueOf(-1);
    private static final Collection<String> TWO_D_IDENTIFIERS = Arrays.asList("2d", "2dsphere");

    private final List<IndexField> indexFields;

    private final String name;
    private final boolean unique;
    private final boolean sparse;
    private final String language;
    private final boolean hidden;
    private Duration expireAfter;
    private String partialFilterExpression;
    private Document collation;
    private Document wildcardProjection;

    public IndexInfo(List<IndexField> indexFields, String name, boolean unique, boolean sparse, String language) {

        this.indexFields = Collections.unmodifiableList(indexFields);
        this.name = name;
        this.unique = unique;
        this.sparse = sparse;
        this.language = language;
        this.hidden = false;
    }

    public IndexInfo(List<IndexField> indexFields, String name, boolean unique, boolean sparse, String language, boolean hidden) {

        this.indexFields = Collections.unmodifiableList(indexFields);
        this.name = name;
        this.unique = unique;
        this.sparse = sparse;
        this.language = language;
        this.hidden = hidden;
    }

    /**
     * Creates new {@link IndexInfo} parsing required properties from the given {@literal sourceDocument}.
     *
     * @param sourceDocument never {@literal null}.
     * @return new instance of {@link IndexInfo}.
     * @since 1.10
     */
    public static IndexInfo indexInfoOf(Document sourceDocument) {

        Document keyDbObject = (Document) sourceDocument.get("key");
        int numberOfElements = keyDbObject.keySet().size();

        List<IndexField> indexFields = new ArrayList<IndexField>(numberOfElements);

        for (String key : keyDbObject.keySet()) {

            Object value = keyDbObject.get(key);

            if (TWO_D_IDENTIFIERS.contains(value)) {

                indexFields.add(IndexField.geo(key));

            } else if ("text".equals(value)) {

                Document weights = (Document) sourceDocument.get("weights");

                for (String fieldName : weights.keySet()) {
                    indexFields.add(IndexField.text(fieldName, Float.valueOf(weights.get(fieldName).toString())));
                }

            } else {

                if (ObjectUtils.nullSafeEquals("hashed", value)) {
                    indexFields.add(IndexField.hashed(key));
                } else if (key.endsWith("$**")) {
                    indexFields.add(IndexField.wildcard(key));
                } else {

                    Double keyValue = Double.valueOf(value.toString());

                    if (ONE.equals(keyValue)) {
                        indexFields.add(IndexField.create(key, ASC));
                    } else if (MINUS_ONE.equals(keyValue)) {
                        indexFields.add(IndexField.create(key, DESC));
                    }
                }
            }
        }

        String name = sourceDocument.get("name").toString();

        boolean unique = sourceDocument.get("unique", false);
        boolean sparse = sourceDocument.get("sparse", false);
        boolean hidden = sourceDocument.getBoolean("hidden", false);
        String language = sourceDocument.containsKey("default_language") ? sourceDocument.getString("default_language")
                : "";

        String partialFilter = extractPartialFilterString(sourceDocument);

        IndexInfo info = new IndexInfo(indexFields, name, unique, sparse, language, hidden);
        info.partialFilterExpression = partialFilter;
        info.collation = sourceDocument.get("collation", Document.class);

        if (sourceDocument.containsKey("expireAfterSeconds")) {

            Number expireAfterSeconds = sourceDocument.get("expireAfterSeconds", Number.class);
            info.expireAfter = Duration.ofSeconds(NumberUtils.convertNumberToTargetClass(expireAfterSeconds, Long.class));
        }

        if (sourceDocument.containsKey("wildcardProjection")) {
            info.wildcardProjection = sourceDocument.get("wildcardProjection", Document.class);
        }

        return info;
    }

    /**
     * @param sourceDocument never {@literal null}.
     * @return the {@link String} representation of the partial filter {@link Document}.
     * @since 2.1.11
     */
    private static String extractPartialFilterString(Document sourceDocument) {

        if (!sourceDocument.containsKey("partialFilterExpression")) {
            return null;
        }

        return SerializationUtils.serializeToJsonSafely(sourceDocument.get("partialFilterExpression", Document.class));
    }

    /**
     * Returns the individual index fields of the index.
     *
     * @return
     */
    public List<IndexField> getIndexFields() {
        return this.indexFields;
    }

    /**
     * Returns whether the index is covering exactly the fields given independently of the order.
     *
     * @param keys must not be {@literal null}.
     * @return
     */
    public boolean isIndexForFields(Collection<String> keys) {

        if (keys == null) {
            throw new IllegalArgumentException("Collection of keys must not be null");
        }

        List<String> indexKeys = new ArrayList<String>(indexFields.size());

        for (IndexField field : indexFields) {
            indexKeys.add(field.getKey());
        }

        return indexKeys.containsAll(keys);
    }

    public String getName() {
        return name;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isSparse() {
        return sparse;
    }

    /**
     * @return
     * @since 1.6
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @return
     * @since 1.0
     */
    public String getPartialFilterExpression() {
        return partialFilterExpression;
    }

    /**
     * Get collation information.
     *
     * @return
     * @since 2.0
     */
    public Optional<Document> getCollation() {
        return Optional.ofNullable(collation);
    }

    /**
     * Get {@literal wildcardProjection} information.
     *
     * @return {@link Optional#empty() empty} if not set.
     * @since 3.3
     */
    public Optional<Document> getWildcardProjection() {
        return Optional.ofNullable(wildcardProjection);
    }

    /**
     * Get the duration after which documents within the index expire.
     *
     * @return the expiration time if set, {@link Optional#empty()} otherwise.
     * @since 2.2
     */
    public Optional<Duration> getExpireAfter() {
        return Optional.ofNullable(expireAfter);
    }

    /**
     * @return {@literal true} if a hashed index field is present.
     * @since 2.2
     */
    public boolean isHashed() {
        return getIndexFields().stream().anyMatch(IndexField::isHashed);
    }

    /**
     * @return {@literal true} if a wildcard index field is present.
     * @since 3.3
     */
    public boolean isWildcard() {
        return getIndexFields().stream().anyMatch(IndexField::isWildcard);
    }

    public boolean isHidden() {
        return hidden;
    }


    @Override
    public String toString() {

        return "IndexInfo [indexFields=" + indexFields + ", name=" + name + ", unique=" + unique + ", sparse=" + sparse
                + ", language=" + language + ", partialFilterExpression=" + partialFilterExpression + ", collation=" + collation
                + ", expireAfterSeconds=" + ObjectUtils.nullSafeToString(expireAfter) + ", hidden=" + hidden + "]";
    }

    @Override
    public int hashCode() {

        int result = 17;
        result += 31 * ObjectUtils.nullSafeHashCode(indexFields);
        result += 31 * ObjectUtils.nullSafeHashCode(name);
        result += 31 * ObjectUtils.nullSafeHashCode(unique);
        result += 31 * ObjectUtils.nullSafeHashCode(sparse);
        result += 31 * ObjectUtils.nullSafeHashCode(language);
        result += 31 * ObjectUtils.nullSafeHashCode(partialFilterExpression);
        result += 31 * ObjectUtils.nullSafeHashCode(collation);
        result += 31 * ObjectUtils.nullSafeHashCode(expireAfter);
        result += 31 * ObjectUtils.nullSafeHashCode(hidden);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IndexInfo other = (IndexInfo) obj;
        if (indexFields == null) {
            if (other.indexFields != null) {
                return false;
            }
        } else if (!indexFields.equals(other.indexFields)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (sparse != other.sparse) {
            return false;
        }
        if (unique != other.unique) {
            return false;
        }
        if (!ObjectUtils.nullSafeEquals(language, other.language)) {
            return false;
        }
        if (!ObjectUtils.nullSafeEquals(partialFilterExpression, other.partialFilterExpression)) {
            return false;
        }
        if (!ObjectUtils.nullSafeEquals(collation, other.collation)) {
            return false;
        }
        if (!ObjectUtils.nullSafeEquals(expireAfter, other.expireAfter)) {
            return false;
        }
        if (hidden != other.hidden) {
            return false;
        }
        return true;
    }

}
