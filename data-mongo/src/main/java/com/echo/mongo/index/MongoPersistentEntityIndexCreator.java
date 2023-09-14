package com.echo.mongo.index;

import com.echo.common.util.ObjectUtils;
import com.echo.common.util.StringUtils;
import com.echo.mongo.core.IndexOperationsProvider;
import com.echo.mongo.excetion.InvalidDataAccessApiUsageException;
import com.echo.mongo.excetion.InvalidMongoDbApiUsageException;
import com.echo.mongo.mapping.MongoMappingContext;
import com.echo.mongo.mapping.MongoPersistentEntity;
import com.echo.mongo.mapping.MongoPersistentProperty;
import com.echo.mongo.mapping.anno.Document;
import com.echo.mongo.util.BsonUtils;
import com.mongodb.MongoException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * mongodb 索引创建器
 *
 * @author: li-yuanwen
 */
@Slf4j
public class MongoPersistentEntityIndexCreator {


    private final Map<Class<?>, Boolean> classesSeen = new ConcurrentHashMap<Class<?>, Boolean>();
    private final MongoMappingContext mappingContext;

    private final IndexOperationsProvider indexOperationsProvider;

    public MongoPersistentEntityIndexCreator(MongoMappingContext mappingContext, IndexOperationsProvider indexOperationsProvider) {

        if (mappingContext == null) {
            throw new IllegalArgumentException("MongoMappingContext must not be null");
        }
        if (indexOperationsProvider == null) {
            throw new IllegalArgumentException("IndexOperationsProvider must not be null");
        }

        this.mappingContext = mappingContext;
        this.indexOperationsProvider = indexOperationsProvider;

        for (MongoPersistentEntity persistentEntity : mappingContext.getPersistentEntities()) {
            checkForIndexes(persistentEntity);
        }

    }


    private void checkForIndexes(final MongoPersistentEntity entity) {

        Class<?> type = entity.getType();

        if (!classesSeen.containsKey(type)) {

            this.classesSeen.put(type, Boolean.TRUE);

            if (log.isDebugEnabled()) {
                log.debug("Analyzing class " + type + " for index information");
            }

            checkForAndCreateIndexes(entity);
        }
    }

    private void checkForAndCreateIndexes(MongoPersistentEntity entity) {
        if (!entity.isAnnotationPresent(Document.class)) {
            return;
        }

        for (IndexDefinitionHolder indexDefinition : resolveIndexForEntity(entity)) {
            createIndex(indexDefinition);
        }
    }

    void createIndex(IndexDefinitionHolder indexDefinition) {

        try {

            IndexOperations indexOperations = indexOperationsProvider.indexOps(indexDefinition.getCollection());
            indexOperations.ensureIndex(indexDefinition);

        } catch (Exception ex) {

            if (ex.getCause() instanceof MongoException) {

                IndexInfo existingIndex = fetchIndexInformation(indexDefinition);
                String message = "Cannot create index for '%s' in collection '%s' with keys '%s' and options '%s'";

                if (existingIndex != null) {
                    message += " Index already defined as '%s'";
                }

                throw new InvalidMongoDbApiUsageException(
                        String.format(message, indexDefinition.getCollection(),
                                indexDefinition.getIndexKeys(), indexDefinition.getIndexOptions(), existingIndex),
                        ex.getCause()) {
                };
            }

            throw ex;
        }
    }

    public List<IndexDefinitionHolder> resolveIndexForEntity(MongoPersistentEntity root) {

        if (root == null) {
            throw new IllegalArgumentException("MongoPersistentEntity must not be null");
        }

        Document document = root.findAnnotation(Document.class);
        if (document == null) {
            throw new IllegalArgumentException(String
                    .format("Entity %s is not a collection root; Make sure to annotate it with @Document"
                            , root.getClass().getName()));
        }

        String collectionName = root.getCollectionName();

        List<IndexDefinitionHolder> indexInformation = new ArrayList<>(tryCreateCompoundIndexDefinitions(root));

        root.doWithProperties(new PropertyHandler() {
            @Override
            public void doWithPersistentProperty(MongoPersistentProperty property) {
                if (property.findAnnotation(Indexed.class) != null) {
                    indexInformation.add(createIndexDefinition(collectionName, property));
                }
            }
        });

        return indexInformation;
    }

    protected IndexDefinitionHolder createIndexDefinition(String collection,
                                                          MongoPersistentProperty persistentProperty) {

        Indexed index = persistentProperty.findAnnotation(Indexed.class);

        if (index == null) {
            return null;
        }

        Index indexDefinition = new Index().on(persistentProperty.getFieldName(),
                IndexDirection.ASCENDING.equals(index.direction()) ? Direction.ASC : Direction.DESC);

        if (!index.useGeneratedName()) {
            indexDefinition
                    .named(pathAwareIndexName(index.name(), persistentProperty.getOwner(), persistentProperty));
        }

        if (index.unique()) {
            indexDefinition.unique();
        }

        if (index.sparse()) {
            indexDefinition.sparse();
        }

        if (index.background()) {
            indexDefinition.background();
        }

        if (index.expireAfterSeconds() >= 0) {
            indexDefinition.expire(index.expireAfterSeconds(), TimeUnit.SECONDS);
        }

        if (StringUtils.hasLength(index.partialFilter())) {
            indexDefinition.partial(evaluatePartialFilter(index.partialFilter(), persistentProperty.getOwner()));
        }

        return new IndexDefinitionHolder(indexDefinition, collection);
    }

    private List<IndexDefinitionHolder> tryCreateCompoundIndexDefinitions(MongoPersistentEntity entity) {
        if (!entity.isAnnotationPresent(CompoundIndexes.class) && !entity.isAnnotationPresent(CompoundIndex.class)) {
            return Collections.emptyList();
        }
        return createCompoundIndexDefinitions(entity);
    }

    private List<IndexDefinitionHolder> createCompoundIndexDefinitions(MongoPersistentEntity entity) {
        List<IndexDefinitionHolder> indexDefinitions = new ArrayList<>();
        CompoundIndexes indexes = entity.findAnnotation(CompoundIndexes.class);

        if (indexes != null) {
            indexDefinitions = Arrays.stream(indexes.value())
                    .map(index -> createCompoundIndexDefinition(entity, index))
                    .collect(Collectors.toList());
        }

        CompoundIndex index = entity.findAnnotation(CompoundIndex.class);

        if (index != null) {
            indexDefinitions.add(createCompoundIndexDefinition(entity, index));
        }

        return indexDefinitions;
    }

    private IndexDefinitionHolder createCompoundIndexDefinition(MongoPersistentEntity entity, CompoundIndex compoundIndex) {
        CompoundIndexDefinition indexDefinition = new CompoundIndexDefinition(
                resolveCompoundIndexKeyFromStringDefinition(compoundIndex.def(), entity));

        if (!compoundIndex.useGeneratedName()) {
            indexDefinition.named(pathAwareIndexName(compoundIndex.name(), entity, null));
        }

        if (compoundIndex.unique()) {
            indexDefinition.unique();
        }

        if (compoundIndex.sparse()) {
            indexDefinition.sparse();
        }

        if (compoundIndex.background()) {
            indexDefinition.background();
        }
        if (StringUtils.hasLength(compoundIndex.partialFilter())) {
            indexDefinition.partial(evaluatePartialFilter(compoundIndex.partialFilter(), entity));
        }

        return new IndexDefinitionHolder(indexDefinition, entity.getCollectionName());
    }

    private String pathAwareIndexName(String indexName, MongoPersistentEntity entity,
                                      MongoPersistentProperty property) {
        String nameToUse = "";
        if (StringUtils.hasLength(indexName)) {

            nameToUse = indexName;
        }
        return nameToUse;

    }

    private PartialIndexFilter evaluatePartialFilter(String filterExpression, MongoPersistentEntity entity) {
        return PartialIndexFilter.of(BsonUtils.parse(filterExpression, null));
    }


    private org.bson.Document resolveCompoundIndexKeyFromStringDefinition(String keyDefinitionString,
                                                                          MongoPersistentEntity entity) {
        if (!StringUtils.hasLength(keyDefinitionString)) {
            throw new InvalidDataAccessApiUsageException("Cannot create index on root level for empty keys");
        }

        return org.bson.Document.parse(keyDefinitionString);
    }

    private IndexInfo fetchIndexInformation(IndexDefinitionHolder indexDefinition) {

        if (indexDefinition == null) {
            return null;
        }

        try {

            IndexOperations indexOperations = indexOperationsProvider.indexOps(indexDefinition.getCollection());
            Object indexNameToLookUp = indexDefinition.getIndexOptions().get("name");

            List<IndexInfo> existingIndexes = indexOperations.getIndexInfo();

            return existingIndexes.stream().//
                    filter(indexInfo -> ObjectUtils.nullSafeEquals(indexNameToLookUp, indexInfo.getName())).//
                    findFirst().//
                    orElse(null);

        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(
                        String.format("Failed to load index information for collection '%s'", indexDefinition.getCollection()), e);
            }
        }

        return null;
    }
}
