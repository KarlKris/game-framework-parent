package com.echo.mongo.index;

import com.echo.common.util.NumberUtils;
import com.echo.common.util.StringUtils;
import com.echo.mongo.core.CollectionCallback;
import com.echo.mongo.core.MongoOperations;
import com.echo.mongo.core.QueryOperations;
import com.echo.mongo.excetion.InvalidMongoDbApiUsageException;
import com.echo.mongo.mapping.MongoPersistentEntity;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Default implementation of {@link IndexOperations}.
 *
 * @author: li-yuanwen
 */
public class DefaultIndexOperations implements IndexOperations {

    private static final String PARTIAL_FILTER_EXPRESSION_KEY = "partialFilterExpression";

    private final String collectionName;

    private final Class<?> type;

    private final MongoOperations mongoOperations;

    private final QueryOperations queryOperations;

    public DefaultIndexOperations(String collectionName, MongoOperations mongoOperations, QueryOperations queryOperations) {
        this(collectionName, null, mongoOperations, queryOperations);
    }

    public DefaultIndexOperations(String collectionName, Class<?> type, MongoOperations mongoOperations, QueryOperations queryOperations) {

        if (mongoOperations == null) {
            throw new IllegalArgumentException("MongoOperations must not be null");
        }
        if (queryOperations == null) {
            throw new IllegalArgumentException("QueryOperations must not be null");
        }
        if (!StringUtils.hasLength(collectionName)) {
            throw new IllegalArgumentException("Collection name must not be null or empty");
        }

        this.collectionName = collectionName;
        this.type = type;
        this.mongoOperations = mongoOperations;
        this.queryOperations = queryOperations;
    }

    @Override
    public String ensureIndex(IndexDefinition indexDefinition) {
        return execute(collection -> {

            MongoPersistentEntity entity = lookupPersistentEntity(type, collectionName);

            IndexOptions indexOptions = IndexConverters.indexDefinitionToIndexOptionsConverter().convert(indexDefinition);

            indexOptions = addPartialFilterIfPresent(indexOptions, indexDefinition.getIndexOptions(), entity);

            Document mappedKeys = queryOperations.getMappedSort(indexDefinition.getIndexKeys(), entity);
            return collection.createIndex(mappedKeys, indexOptions);
        });
    }


    @Override
    public void alterIndex(String name, com.echo.mongo.index.IndexOptions options) {
        Document indexOptions = new Document("name", name);
        indexOptions.putAll(options.toDocument());

        Document result = mongoOperations
                .execute(db -> db.runCommand(new Document("collMod", collectionName).append("index", indexOptions)));

        if (NumberUtils.convertNumberToTargetClass(result.get("ok", (Number) 0), Integer.class) != 1) {
            throw new InvalidMongoDbApiUsageException(String.format("Index '%s' could not be modified. Response was %s", name, result.toJson()), null);
        }
    }

    @Override
    public void dropIndex(String name) {
        execute(collection -> {
            collection.dropIndex(name);
            return null;
        });

    }

    @Override
    public void dropAllIndexes() {
        dropIndex("*");
    }

    @Override
    public List<IndexInfo> getIndexInfo() {
        return execute(new CollectionCallback<List<IndexInfo>>() {

            public List<IndexInfo> doInCollection(MongoCollection<Document> collection)
                    throws MongoException {

                MongoCursor<Document> cursor = collection.listIndexes(Document.class).iterator();
                return getIndexData(cursor);
            }

            private List<IndexInfo> getIndexData(MongoCursor<Document> cursor) {

                int available = cursor.available();
                List<IndexInfo> indexInfoList = available > 0 ? new ArrayList<>(available) : new ArrayList<>();

                while (cursor.hasNext()) {

                    Document ix = cursor.next();
                    IndexInfo indexInfo = IndexConverters.documentToIndexInfoConverter().convert(ix);
                    indexInfoList.add(indexInfo);
                }

                return indexInfoList;
            }
        });
    }

    public <T> T execute(CollectionCallback<T> callback) {

        if (callback == null) {
            throw new IllegalArgumentException("CollectionCallback must not be null");
        }

        if (type != null) {
            return mongoOperations.execute(type, callback);
        }

        return mongoOperations.execute(collectionName, callback);
    }

    private MongoPersistentEntity lookupPersistentEntity(Class<?> entityType, String collection) {

        if (entityType != null) {
            return queryOperations.getMappingContext().getRequiredPersistentEntity(entityType);
        }

        Collection<? extends MongoPersistentEntity> entities = queryOperations.getMappingContext().getPersistentEntities();

        for (MongoPersistentEntity entity : entities) {
            if (entity.getCollectionName().equals(collection)) {
                return entity;
            }
        }

        return null;
    }

    private IndexOptions addPartialFilterIfPresent(IndexOptions ops, Document sourceOptions,
                                                   MongoPersistentEntity entity) {

        if (!sourceOptions.containsKey(PARTIAL_FILTER_EXPRESSION_KEY)) {
            return ops;
        }

        Object obj = sourceOptions.get(PARTIAL_FILTER_EXPRESSION_KEY);

        if (!(obj instanceof Document)) {
            String msg = obj.getClass().getName() + "] must be an instance of " + Document.class;
            throw new IllegalArgumentException(msg);
        }

        return ops.partialFilterExpression(queryOperations.getMappedSort((Document) obj, entity));
    }


}
