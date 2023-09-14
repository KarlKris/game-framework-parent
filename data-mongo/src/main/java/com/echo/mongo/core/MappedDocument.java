package com.echo.mongo.core;

import com.echo.common.util.StreamUtils;
import com.echo.mongo.query.Update;
import com.echo.mongo.query.UpdateDefinition;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Collection;
import java.util.List;

/**
 * A MongoDB document in its mapped state. I.e. after a source document has been mapped using mapping information of the
 * entity the source document was supposed to represent.
 *
 * @author: li-yuanwen
 */
public class MappedDocument {

    private static final String ID_FIELD = "_id";
    private static final Document ID_ONLY_PROJECTION = new Document(ID_FIELD, 1);

    private final Document document;

    private MappedDocument(Document document) {
        this.document = document;
    }

    public static MappedDocument of(Document document) {
        return new MappedDocument(document);
    }

    public static Document getIdOnlyProjection() {
        return ID_ONLY_PROJECTION;
    }

    public static Document getIdIn(Collection<?> ids) {
        return new Document(ID_FIELD, new Document("$in", ids));
    }

    public static List<Object> toIds(Collection<Document> documents) {

        return documents.stream()//
                .map(it -> it.get(ID_FIELD))//
                .collect(StreamUtils.toUnmodifiableList());
    }

    public boolean hasId() {
        return document.containsKey(ID_FIELD);
    }

    public boolean hasNonNullId() {
        return hasId() && document.get(ID_FIELD) != null;
    }

    public Object getId() {
        return document.get(ID_FIELD);
    }

    public <T> T getId(Class<T> type) {
        return document.get(ID_FIELD, type);
    }

    public boolean isIdPresent(Class<?> type) {
        return type.isInstance(getId());
    }

    public Bson getIdFilter() {
        return new Document(ID_FIELD, document.get(ID_FIELD));
    }

    public Object get(String key) {
        return document.get(key);
    }

    public UpdateDefinition updateWithoutId() {
        return new MappedUpdate(Update.fromDocument(document, ID_FIELD));
    }

    public Document getDocument() {
        return this.document;
    }

    /**
     * Updates the documents {@link #ID_FIELD}.
     *
     * @param value the {@literal _id} value to set.
     * @since 3.4.3
     */
    public void updateId(Object value) {
        document.put(ID_FIELD, value);
    }

    /**
     * An {@link UpdateDefinition} that indicates that the {@link #getUpdateObject() update object} has already been
     * mapped to the specific domain type.
     *
     * @author Christoph Strobl
     * @since 2.2
     */
    class MappedUpdate implements UpdateDefinition {

        private final Update delegate;

        MappedUpdate(Update delegate) {
            this.delegate = delegate;
        }

        @Override
        public Document getUpdateObject() {
            return delegate.getUpdateObject();
        }

        @Override
        public boolean modifies(String key) {
            return delegate.modifies(key);
        }

        @Override
        public void inc(String version) {
            delegate.inc(version);
        }

        @Override
        public Boolean isIsolated() {
            return delegate.isIsolated();
        }

        @Override
        public List<ArrayFilter> getArrayFilters() {
            return delegate.getArrayFilters();
        }

        @Override
        public boolean hasArrayFilters() {
            return delegate.hasArrayFilters();
        }
    }
}
