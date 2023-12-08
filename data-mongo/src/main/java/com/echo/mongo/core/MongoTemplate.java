package com.echo.mongo.core;

import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.ClassUtils;
import com.echo.common.util.ObjectUtils;
import com.echo.common.util.StringUtils;
import com.echo.mongo.MongoDatabaseFactory;
import com.echo.mongo.convert.EntityReader;
import com.echo.mongo.convert.EntityWriter;
import com.echo.mongo.convert.GenericMongoConverter;
import com.echo.mongo.excetion.DataAccessException;
import com.echo.mongo.excetion.MappingException;
import com.echo.mongo.index.DefaultIndexOperations;
import com.echo.mongo.index.IndexOperations;
import com.echo.mongo.index.MongoPersistentEntityIndexCreator;
import com.echo.mongo.mapping.InstanceWrapper;
import com.echo.mongo.mapping.MongoPersistentEntity;
import com.echo.mongo.mapping.MongoPersistentProperty;
import com.echo.mongo.query.BasicQuery;
import com.echo.mongo.query.Query;
import com.echo.mongo.query.UpdateDefinition;
import com.echo.mongo.util.BsonUtils;
import com.echo.mongo.util.SerializationUtils;
import com.mongodb.MongoException;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.*;

/**
 * mongodb 操作模板
 *
 * @author: li-yuanwen
 */
@Slf4j
public class MongoTemplate implements MongoOperations, IndexOperationsProvider {

    private static final Document ID_ONLY_PROJECTION = new Document(MongoPersistentProperty.ID_FIELD_NAME, 1);

    private final MongoDatabaseFactory mongoDbFactory;

    private final EntityOperations entityOperations;
    private final QueryOperations queryOperations;

    private final GenericMongoConverter mongoConverter;

    private final MongoPersistentEntityIndexCreator indexCreator;

    private ReadPreference readPreference;
    private WriteConcern writeConcern;

    public MongoTemplate(MongoDatabaseFactory mongoDbFactory, GenericMongoConverter mongoConverter) {
        this.mongoDbFactory = mongoDbFactory;
        this.mongoConverter = mongoConverter;
        this.entityOperations = new EntityOperations(mongoConverter.getMappingContext());
        this.queryOperations = new QueryOperations(mongoConverter, mongoConverter.getMappingContext()
                , mongoConverter.getConversionService(), mongoDbFactory);
        if (queryOperations.getMappingContext().isAutoIndexCreation()) {
            indexCreator = new MongoPersistentEntityIndexCreator(queryOperations.getMappingContext(), this);
        } else {
            indexCreator = null;
        }
    }


    public MongoDatabase getMongoDatabase() {
        return doGetDatabase();
    }

    public EntityOperations getEntityOperations() {
        return entityOperations;
    }

    public void setReadPreference(ReadPreference readPreference) {
        this.readPreference = readPreference;
    }

    public void setWriteConcern(WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
    }

    // ---------------------- IndexOperationsProvider ------------------------------------------------------------------

    @Override
    public IndexOperations indexOps(String collectionName, Class<?> type) {
        return new DefaultIndexOperations(collectionName, type, this, queryOperations);
    }


    // ---------------------- MongoOperations --------------------------------------------------------------------------

    @Override
    public String getCollectionName(Class<?> entityClass) {
        return entityOperations.determineCollectionName(entityClass);
    }

    @Override
    public <T> T execute(DbCallback<T> action) {

        if (action == null) {
            throw new IllegalArgumentException("DbCallback must not be null");
        }

        try {
            MongoDatabase db = doGetDatabase();
            return action.doInDB(db);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @Override
    public <T> T execute(Class<?> entityClass, CollectionCallback<T> action) {
        if (entityClass == null) {
            throw new IllegalArgumentException("EntityClass must not be null");
        }
        return execute(getCollectionName(entityClass), action);
    }

    @Override
    public <T> T execute(String collectionName, CollectionCallback<T> action) {
        if (!StringUtils.hasLength(collectionName)) {
            throw new IllegalArgumentException("CollectionName must not be null");
        }
        if (action == null) {
            throw new IllegalArgumentException("CollectionCallback must not be null");
        }
        MongoCollection<Document> collection = getAndPrepareCollection(doGetDatabase(), collectionName);
        return action.doInCollection(collection);
    }

    @Override
    public <T> T save(T objectToSave) {
        return save(objectToSave, getCollectionName(ClassUtils.getUserClass(objectToSave)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T save(T objectToSave, String collectionName) {
        if (objectToSave == null) {
            throw new IllegalArgumentException("Object to save must not be null");
        }
        if (!StringUtils.hasLength(collectionName)) {
            throw new IllegalArgumentException("Collection name must not be null or empty");
        }
        ensureNotCollectionLike(objectToSave);
        return (T) doSave(collectionName, objectToSave, this.mongoConverter);
    }

    protected <T> T doSave(String collectionName, T objectToSave, EntityWriter<T> writer) {

        MongoPersistentEntity entity = entityOperations.getPersistentEntity(objectToSave.getClass());

        Document dbDoc = entity.toMappingDocument(objectToSave, writer);

        Object id = saveDocument(collectionName, dbDoc, objectToSave.getClass());

        return populateIdIfNecessary(objectToSave, id);
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

    @Override
    public <T> T findOne(Query query, Class<T> entityClass) {
        return findOne(query, entityClass, getCollectionName(entityClass));
    }

    @Override
    public <T> T findOne(Query query, Class<T> entityClass, String collectionName) {

        if (query == null) {
            throw new IllegalArgumentException("Query must not be null");
        }
        if (entityClass == null) {
            throw new IllegalArgumentException("EntityClass must not be null");
        }
        if (collectionName == null) {
            throw new IllegalArgumentException("CollectionName must not be null");
        }

        return doFindOne(collectionName, query.getQueryObject(), query.getFieldsObject(),
                new QueryCursorPreparer(query, entityClass), entityClass);
    }

    @Override
    public <T> T findById(Object id, Class<T> entityClass) {
        return findById(id, entityClass, getCollectionName(entityClass));
    }

    @Override
    public <T> T findById(Object id, Class<T> entityClass, String collectionName) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        if (entityClass == null) {
            throw new IllegalArgumentException("EntityClass must not be null");
        }
        if (collectionName == null) {
            throw new IllegalArgumentException("CollectionName must not be null");
        }

        return doFindOne(collectionName, new Document("_id", id), new Document(), CursorPreparer.NO_OP_PREPARER,
                entityClass);
    }


    protected <T> T doFindOne(String collectionName,
                              Document query, Document fields, CursorPreparer preparer, Class<T> entityClass) {

        MongoPersistentEntity entity = entityOperations.getPersistentEntity(entityClass);

        QueryOperations.QueryContext queryContext = queryOperations.createQueryContext(new BasicQuery(query, fields));
        Document mappedFields = queryContext.getMappedFields(entity);
        Document mappedQuery = queryContext.getMappedQuery(entity);

        if (log.isDebugEnabled()) {
            log.debug(String.format("findOne using query: %s fields: %s for class: %s in collection: %s",
                    toJsonSafely(query), mappedFields, entityClass, collectionName));
        }

        return executeFindOneInternal(new FindOneCallback(mappedQuery, mappedFields, preparer),
                new ReadDocumentCallback<>(this.mongoConverter, entityClass, collectionName), collectionName);
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

    private <T> T executeFindOneInternal(CollectionCallback<Document> collectionCallback,
                                         DocumentCallback<T> documentCallback, String collectionName) {
        Document document = collectionCallback.doInCollection(getAndPrepareCollection(doGetDatabase(), collectionName));
        return document != null ? documentCallback.doWith(document) : null;
    }

    protected MongoDatabase doGetDatabase() {
        return mongoDbFactory.getMongoDatabase();
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
     * Ensure the given {@literal source} is not an {@link java.lang.reflect.Array}, {@link Collection} or
     * {@link Iterator}.
     *
     * @param source can be {@literal null}.
     * @since 3.2.
     */
    protected void ensureNotCollectionLike(Object source) {
        if (TypeDescriptor.forObject(source).isCollectionLike()) {
            throw new IllegalArgumentException("Cannot use a collection here");
        }
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

    private static class FindOneCallback implements CollectionCallback<Document> {

        private final Document query;
        private final Document fields;

        private final CursorPreparer cursorPreparer;

        public FindOneCallback(Document query, Document fields, CursorPreparer cursorPreparer) {
            this.query = query;
            this.fields = fields;
            this.cursorPreparer = cursorPreparer;
        }

        @Override
        public Document doInCollection(MongoCollection<Document> collection) throws MongoException, DataAccessException {
            FindIterable<Document> iterable = cursorPreparer.prepare(collection.find(query, Document.class));
            if (fields != null) {
                iterable.projection(fields);
            }
            return iterable.first();
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
        if (objectToSave == null) {
            throw new IllegalArgumentException("ObjectToSave must not be null");
        }
        return insert(objectToSave, getCollectionName(objectToSave.getClass()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T insert(T objectToSave, String collectionName) {
        if (objectToSave == null) {
            throw new IllegalArgumentException("ObjectToSave must not be null");
        }
        if (!StringUtils.hasLength(collectionName)) {
            throw new IllegalArgumentException("CollectionName must not be null or blank");
        }

        TypeDescriptor typeDescriptor = TypeDescriptor.forObject(objectToSave);
        if (typeDescriptor.isCollectionLike()) {
            throw new IllegalArgumentException("Cannot use a collection here");
        }
        return (T) doInsert(collectionName, objectToSave, this.mongoConverter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> insert(Collection<? extends T> batchToSave, Class<?> entityClass) {

        if (batchToSave == null) {
            throw new IllegalArgumentException("BatchToSave must not be null");
        }

        return (Collection<T>) doInsertBatch(getCollectionName(entityClass), batchToSave, this.mongoConverter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> insert(Collection<? extends T> batchToSave, String collectionName) {

        if (batchToSave == null) {
            throw new IllegalArgumentException("BatchToSave must not be null");
        }
        if (collectionName == null) {
            throw new IllegalArgumentException("CollectionName must not be null");
        }

        return (Collection<T>) doInsertBatch(collectionName, batchToSave, this.mongoConverter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> insertAll(Collection<? extends T> objectsToSave) {
        if (objectsToSave == null) {
            throw new IllegalArgumentException("ObjectsToSave must not be null");
        }

        return (Collection<T>) doInsertAll(objectsToSave, this.mongoConverter);
    }


    @SuppressWarnings("unchecked")
    protected <T> Collection<T> doInsertAll(Collection<? extends T> listToSave, EntityWriter<T> writer) {

        Map<String, List<T>> elementsByCollection = new HashMap<>();
        List<T> savedObjects = new ArrayList<>(listToSave.size());

        for (T element : listToSave) {

            if (element == null) {
                continue;
            }

            String collection = getCollectionName(ClassUtils.getUserClass(element));
            List<T> collectionElements = elementsByCollection.computeIfAbsent(collection, k -> new ArrayList<>());

            collectionElements.add(element);
        }

        for (Map.Entry<String, List<T>> entry : elementsByCollection.entrySet()) {
            savedObjects.addAll((Collection<T>) doInsertBatch(entry.getKey(), entry.getValue(), this.mongoConverter));
        }

        return savedObjects;
    }

    protected <T> Collection<T> doInsertBatch(String collectionName, Collection<? extends T> batchToSave,
                                              EntityWriter<T> writer) {
        if (writer == null) {
            throw new IllegalArgumentException("EntityWriter must not be null");
        }

        List<Document> documentList = new ArrayList<>(batchToSave.size());
        List<T> initializedBatchToSave = new ArrayList<>(batchToSave.size());
        for (T uninitialized : batchToSave) {

            MongoPersistentEntity entity = entityOperations.getPersistentEntity(uninitialized.getClass());
            if (entity == null) {
                throw new MappingException("No mapping metadata found for entity of type " + uninitialized.getClass().getName());
            }

            Document document = entity.toMappingDocument(uninitialized, writer);

            documentList.add(document);
            initializedBatchToSave.add(uninitialized);
        }

        List<Object> ids = insertDocumentList(collectionName, documentList);
        List<T> savedObjects = new ArrayList<>(documentList.size());

        int i = 0;
        for (T obj : initializedBatchToSave) {
            if (i < ids.size()) {
                T saved = populateIdIfNecessary(obj, ids.get(i));
                savedObjects.add(saved);
            } else {
                savedObjects.add(obj);
            }
            i++;
        }
        return savedObjects;
    }

    protected <T> T doInsert(String collectionName, T objectToSave, EntityWriter<T> writer) {
        MongoPersistentEntity entity = entityOperations.getPersistentEntity(objectToSave.getClass());
        if (entity == null) {
            throw new MappingException("No mapping metadata found for entity of type " + objectToSave.getClass().getName());
        }
        Document document = entity.toMappingDocument(objectToSave, writer);
        Object id = insertDocument(collectionName, document, objectToSave.getClass());
        return populateIdIfNecessary(objectToSave, id);
    }

    protected Object insertDocument(String collectionName, Document document, Class<?> entityClass) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Inserting Document containing fields: %s in collection: %s", document.keySet(),
                    collectionName));
        }

        return execute(collectionName, collection -> {
            prepareWriteConcern(collection);
            collection.insertOne(document);
            return document.get(MongoPersistentProperty.ID_FIELD_NAME);
        });
    }

    protected List<Object> insertDocumentList(String collectionName, List<Document> documents) {

        if (documents.isEmpty()) {
            return Collections.emptyList();
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Inserting list of Documents containing %s items", documents.size()));
        }

        execute(collectionName, collection -> {

            prepareWriteConcern(collection);
            collection.insertMany(documents);

            return null;
        });

        return MappedDocument.toIds(documents);
    }

    protected Object saveDocument(String collectionName, Document dbDoc, Class<?> entityClass) {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Saving Document containing fields: %s", dbDoc.keySet()));
        }

        return execute(collectionName, collection -> {

            prepareWriteConcern(collection);

            MappedDocument mapped = MappedDocument.of(dbDoc);

            if (!mapped.hasId()) {

                mapped = queryOperations.createInsertContext(mapped).prepareId(entityClass);
                collection.insertOne(mapped.getDocument());
            } else {

                MongoPersistentEntity entity = entityOperations.getPersistentEntity(entityClass);
                QueryOperations.UpdateContext updateContext = queryOperations.replaceSingleContext(mapped, true);
                Document replacement = updateContext.getMappedUpdate(entity);

                Document filter = updateContext.getMappedQuery(entity);

//                if (updateContext.requiresShardKey(filter, entity)) {
//
//                    if (entity.getShardKey().isImmutable()) {
//                        filter = updateContext.applyShardKey(entity, filter, null);
//                    } else {
//                        filter = updateContext.applyShardKey(entity, filter,
//                                collection.find(filter, Document.class).projection(updateContext.getMappedShardKey(entity)).first());
//                    }
//                }

                collection.replaceOne(filter, replacement, new ReplaceOptions().upsert(true));
            }
            return mapped.getId();
        });
    }


    protected <T> T populateIdIfNecessary(T savedObject, Object id) {
        MongoPersistentEntity entity = entityOperations.getPersistentEntity(savedObject.getClass());
        InstanceWrapper<T> wrapper = new InstanceWrapper<>(savedObject);
        wrapper.setProperty(entity.getIdProperty(), id);
        return savedObject;
    }

    /**
     * Prepare the WriteConcern before any processing is done using it. This allows a convenient way to apply custom
     * settings in sub-classes. <br />
     * In case of using MongoDB Java driver version 3 the returned {@link WriteConcern} will be defaulted to
     * {@link WriteConcern#ACKNOWLEDGED}
     *
     * @return The prepared WriteConcern or null
     */
    protected MongoCollection<Document> prepareWriteConcern(MongoCollection<Document> collection) {
        if (writeConcern != null && collection.getWriteConcern() != writeConcern) {
            collection.withWriteConcern(writeConcern);
        }
        return collection;
    }

    // ------------------------ removeOperations -------------------------------------------

    @Override
    public DeleteResult remove(Object object) {
        return remove(object, getCollectionName(object.getClass()));
    }

    @Override
    public DeleteResult remove(Object object, String collectionName) {
        if (object == null) {
            throw new IllegalArgumentException("Object must not be null");
        }
        if (!StringUtils.hasLength(collectionName)) {
            throw new IllegalArgumentException("Collection name must not be null or empty");
        }
        MongoPersistentEntity entity = entityOperations.getPersistentEntity(object.getClass());
        if (entity == null) {
            throw new MappingException("No mapping metadata found for entity of type " + object.getClass().getName());
        }
        Query query = entity.getRemoveByQuery(object);
        return doRemove(collectionName, query, object.getClass(), false);
    }

    @Override
    public DeleteResult remove(Query query, Class<?> entityClass) {
        return remove(query, entityClass, getCollectionName(entityClass));
    }

    @Override
    public DeleteResult remove(Query query, Class<?> entityClass, String collectionName) {
        return doRemove(collectionName, query, entityClass, true);
    }

    @Override
    public DeleteResult remove(Query query, String collectionName) {
        return doRemove(collectionName, query, null, true);
    }


    protected <T> DeleteResult doRemove(String collectionName, Query query, Class<T> entityClass, boolean multi) {
        if (query == null) {
            throw new IllegalArgumentException("Query must not be null");
        }
        if (!StringUtils.hasLength(collectionName)) {
            throw new IllegalArgumentException("Collection name must not be null or empty");
        }

        MongoPersistentEntity entity = entityClass != null ? entityOperations.getPersistentEntity(entityClass) : null;
        QueryOperations.DeleteContext deleteContext = queryOperations.deleteQueryContext(query);
        Document queryObject = deleteContext.getMappedQuery(entity);
        DeleteOptions options = deleteContext.getDeleteOptions(entityClass);

        return execute(collectionName, new CollectionCallback<DeleteResult>() {
            @Override
            public DeleteResult doInCollection(MongoCollection<Document> collection) throws MongoException, DataAccessException {

                Document removeQuery = queryObject;

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Remove using query: %s in collection: %s.", toJsonSafely(removeQuery),
                            collectionName));
                }

                if (query.getLimit() > 0 || query.getSkip() > 0) {

                    MongoCursor<Document> cursor = new QueryCursorPreparer(query, entityClass)
                            .prepare(collection.find(removeQuery).projection(ID_ONLY_PROJECTION)) //
                            .iterator();

                    Set<Object> ids = new LinkedHashSet<>();
                    while (cursor.hasNext()) {
                        ids.add(cursor.next().get(MongoPersistentProperty.ID_FIELD_NAME));
                    }

                    removeQuery = new Document(MongoPersistentProperty.ID_FIELD_NAME, new Document("$in", ids));
                }

                MongoCollection<Document> collectionToUse = writeConcern != null
                        ? collection.withWriteConcern(writeConcern)
                        : collection;

                return multi ? collectionToUse.deleteMany(removeQuery, options)
                        : collectionToUse.deleteOne(removeQuery, options);
            }
        });
    }

    // ------------------------ updateOperations -------------------------------------------


    @Override
    public UpdateResult upsert(Query query, UpdateDefinition update, Class<?> entityClass) {
        return doUpdate(getCollectionName(entityClass), query, update, entityClass, true, false);
    }

    @Override
    public UpdateResult upsert(Query query, UpdateDefinition update, String collectionName) {
        return doUpdate(collectionName, query, update, null, true, false);
    }

    @Override
    public UpdateResult upsert(Query query, UpdateDefinition update, Class<?> entityClass, String collectionName) {

        if (entityClass == null) {
            throw new IllegalArgumentException("EntityClass must not be null");
        }

        return doUpdate(collectionName, query, update, entityClass, true, false);
    }

    @Override
    public UpdateResult updateFirst(Query query, UpdateDefinition update, Class<?> entityClass) {
        return doUpdate(getCollectionName(entityClass), query, update, entityClass, false, false);
    }

    @Override
    public UpdateResult updateFirst(Query query, UpdateDefinition update, String collectionName) {
        return doUpdate(collectionName, query, update, null, false, false);
    }

    @Override
    public UpdateResult updateFirst(Query query, UpdateDefinition update, Class<?> entityClass, String collectionName) {

        if (entityClass == null) {
            throw new IllegalArgumentException("EntityClass must not be null");
        }

        return doUpdate(collectionName, query, update, entityClass, false, false);
    }

    @Override
    public UpdateResult updateMulti(Query query, UpdateDefinition update, Class<?> entityClass) {
        return doUpdate(getCollectionName(entityClass), query, update, entityClass, false, true);
    }

    @Override
    public UpdateResult updateMulti(Query query, UpdateDefinition update, String collectionName) {
        return doUpdate(collectionName, query, update, null, false, true);
    }

    @Override
    public UpdateResult updateMulti(Query query, UpdateDefinition update, Class<?> entityClass, String collectionName) {

        if (entityClass == null) {
            throw new IllegalArgumentException("EntityClass must not be null");
        }

        return doUpdate(collectionName, query, update, entityClass, false, true);
    }

    protected UpdateResult doUpdate(String collectionName, Query query, UpdateDefinition update,
                                    Class<?> entityClass, boolean upsert, boolean multi) {

        if (collectionName == null) {
            throw new IllegalArgumentException("CollectionName must not be null");
        }
        if (query == null) {
            throw new IllegalArgumentException("Query must not be null");
        }
        if (update == null) {
            throw new IllegalArgumentException("Update must not be null");
        }

        MongoPersistentEntity entity = entityClass == null ? null : entityOperations.getPersistentEntity(entityClass);
        QueryOperations.UpdateContext updateContext = multi ? queryOperations.updateContext(update, query, upsert)
                : queryOperations.updateSingleContext(update, query, upsert);

        Document queryObj = updateContext.getMappedQuery(entity);
        UpdateOptions opts = updateContext.getUpdateOptions(entityClass);

        Document updateObj = updateContext.getMappedUpdate(entity);


        return execute(collectionName, collection -> {
            prepareWriteConcern(collection);
            if (!QueryOperations.isUpdateObject(updateObj)) {

                Document filter = new Document(queryObj);

//                    if (updateContext.requiresShardKey(filter, entity)) {
//
//                        if (entity.getShardKey().isImmutable()) {
//                            filter = updateContext.applyShardKey(entity, filter, null);
//                        } else {
//                            filter = updateContext.applyShardKey(entity, filter,
//                                    collection.find(filter, Document.class).projection(updateContext.getMappedShardKey(entity)).first());
//                        }
//                    }

                ReplaceOptions replaceOptions = updateContext.getReplaceOptions(entityClass);
                return collection.replaceOne(filter, updateObj, replaceOptions);
            } else {
                return multi ? collection.updateMany(queryObj, updateObj, opts)
                        : collection.updateOne(queryObj, updateObj, opts);
            }
        });
    }

    // ---------------------------------------------------------------------------------


    class QueryCursorPreparer implements CursorPreparer {

        private final Query query;

        private final Document sortObject;

        private final int limit;

        private final long skip;
        private final Class<?> type;

        QueryCursorPreparer(Query query, Class<?> type) {
            this(query, BsonUtils.EMPTY_DOCUMENT, query.getLimit(), query.getSkip(), type);
        }

        QueryCursorPreparer(Query query, Document sortObject, int limit, long skip, Class<?> type) {
            this.query = query;
            this.sortObject = sortObject;
            this.limit = limit;
            this.skip = skip;
            this.type = type;
        }

        @Override
        public FindIterable<Document> prepare(FindIterable<Document> iterable) {

            FindIterable<Document> cursorToUse = iterable;


            HintFunction hintFunction = HintFunction.from(query.getHint());
            if (skip <= 0 && limit <= 0 && ObjectUtils.isEmpty(sortObject) && hintFunction.isEmpty()) {
                return cursorToUse;
            }

            try {
                if (skip > 0) {
                    cursorToUse = cursorToUse.skip((int) skip);
                }
                if (limit > 0) {
                    cursorToUse = cursorToUse.limit(limit);
                }
                if (hintFunction.isPresent()) {
                    cursorToUse = hintFunction.apply(mongoDbFactory, cursorToUse::hintString, cursorToUse::hint);
                }

            } catch (RuntimeException e) {
                throw e;
            }

            return cursorToUse;
        }

    }

    // ---------------------------------------------------------------------------------

    private static String toJsonSafely(Object object) {
        return SerializationUtils.serializeToJsonSafely(object);
    }
}
