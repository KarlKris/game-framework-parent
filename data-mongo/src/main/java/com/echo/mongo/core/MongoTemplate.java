package com.echo.mongo.core;

import com.echo.common.util.JsonUtils;
import com.echo.mongo.convert.EntityReader;
import com.echo.mongo.convert.MongoConverter;
import com.echo.mongo.excetion.DataAccessException;
import com.echo.mongo.excetion.MappingException;
import com.echo.mongo.mapping.MongoPersistentEntity;
import com.echo.mongo.query.BasicQuery;
import com.echo.mongo.query.Query;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.MongoException;
import com.mongodb.ReadPreference;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * mongodb 操作模板
 * @author: li-yuanwen
 */
@Slf4j
public class MongoTemplate implements MongoOperations {

    /** mongodb **/
    private final MongoDatabase mongoDatabase;

    private final EntityOperations entityOperations;
    private final QueryOperations queryOperations;

    private final MongoConverter mongoConverter;

    private ReadPreference readPreference;

    public MongoTemplate(MongoDatabase mongoDatabase, EntityOperations entityOperations
            , QueryOperations queryOperations, MongoConverter mongoConverter) {
        this.mongoDatabase = mongoDatabase;
        this.entityOperations = entityOperations;
        this.queryOperations = queryOperations;
        this.mongoConverter = mongoConverter;
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    // ---------------------- MongoOperations -------------------------

    @Override
    public String getCollectionName(Class<?> entityClass) {
        return entityOperations.determineCollectionName(entityClass);
    }


    // ---------------------- FindOperation ---------------------------

    @Override
    public <T> List<T> find(Query query, Class<T> entityClass) {
        return find(query, entityClass, getCollectionName(entityClass));
    }

    @Override
    public <T> List<T> find(Query query, Class<T> entityClass, String collectionName) {
        return doFind(collectionName, query.getQueryObject(), query.getFieldsObject(), entityClass);
    }

    private <T> List<T> doFind(String collectionName, Document query, Document fields, Class<T> entityClass) {
        MongoPersistentEntity persistentEntity = entityOperations.getPersistentEntity(entityClass);

        QueryOperations.QueryContext context = queryOperations.createQueryContext(new BasicQuery(query, fields));
        Document mappedFields = context.getMappedFields(persistentEntity);
        Document mappedQuery = context.getMappedQuery(persistentEntity);

        if (log.isDebugEnabled()) {
            log.debug(String.format("find using query: %s fields: %s for class: %s in collection: %s",
                    toJsonSafely(mappedQuery), mappedFields, entityClass, collectionName));
        }

        return executeFindMultiInternal(new FindCallback(mappedQuery, mappedFields)
                , new ReadDocumentCallback<>(mongoConverter, entityClass, collectionName), collectionName);
    }

    private <T> List<T> executeFindMultiInternal(CollectionCallback<FindIterable<Document>> collectionCallback
            , DocumentCallback<T> documentCallback, String collectionName) {
        try (MongoCursor<Document> cursor = collectionCallback.doInCollection(getAndPrepareCollection(doGetDatabase()
                , collectionName)).iterator()) {
            int available = cursor.available();
            List<T> result = available > 0 ? new ArrayList<>(available) : new ArrayList<>();

            while (cursor.hasNext()) {
                Document object = cursor.next();
                result.add(documentCallback.doWith(object));
            }
            return result;
        }
    }

    protected MongoDatabase doGetDatabase() {
        return mongoDatabase;
    }

    private MongoCollection<Document> getAndPrepareCollection(MongoDatabase db, String collectionName) {
        MongoCollection<Document> collection = db.getCollection(collectionName, Document.class);
        collection = prepareCollection(collection);
        return collection;
    }

    /**
     * Prepare the collection before any processing is done using it. This allows a convenient way to apply settings like
     * withCodecRegistry() etc. Can be overridden in sub-classes.
     *
     * @param collection
     */
    protected MongoCollection<Document> prepareCollection(MongoCollection<Document> collection) {

        if (this.readPreference != null && this.readPreference != collection.getReadPreference()) {
            return collection.withReadPreference(readPreference);
        }

        return collection;
    }

    /**
     * Simple {@link CollectionCallback} that takes a query {@link Document} plus an optional fields specification
     * {@link Document} and executes that against the {@link com.mongodb.client.MongoCollection}.
     *
     * @author Oliver Gierke
     * @author Thomas Risberg
     * @author Christoph Strobl
     */
    private static class FindCallback implements CollectionCallback<FindIterable<Document>> {

        private final Document query;
        private final Document fields;

        public FindCallback(Document query, Document fields) {

            if (query == null) {
                throw new IllegalArgumentException("Query must not be null");
            }
            if (fields == null) {
                throw new IllegalArgumentException("Fields must not be null");
            }
            this.query = query;
            this.fields = fields;
        }

        @Override
        public FindIterable<Document> doInCollection(MongoCollection<Document> collection)
                throws MongoException, DataAccessException {
            return collection.find(query, Document.class)
                    .projection(fields);
        }
    }


    /**
     * Simple internal callback to allow operations on a {@link Document}.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */

    interface DocumentCallback<T> {

        T doWith(Document object);
    }

    /**
     * Simple {@link DocumentCallback} that will transform {@link Document} into the given target type using the given
     * {@link EntityReader}.
     *
     * @author Oliver Gierke
     * @author Christoph Strobl
     * @author Roman Puchkovskiy
     */
    private class ReadDocumentCallback<T> implements DocumentCallback<T> {

        private final EntityReader<? super T> reader;
        private final Class<T> type;
        private final String collectionName;

        ReadDocumentCallback(EntityReader<? super T> reader, Class<T> type, String collectionName) {

            this.reader = reader;
            this.type = type;
            this.collectionName = collectionName;
        }

        @Override
        public T doWith(Document document) {
            T entity = reader.read(type, document);
            if (entity == null) {
                throw new MappingException(String.format("EntityReader %s returned null", reader));
            }
            return entity;
        }
    }

    // --------------------- insertOperations -----------------------------------------


    @Override
    public <T> T insert(T objectToSave) {
        return null;
    }

    // ---------------------------------------------------------------------------------

    private static String toJsonSafely(Object object) {
        try {
            return JsonUtils.toJson(object);
        } catch (JsonProcessingException e) {
            return object.toString();
        }
    }
}
