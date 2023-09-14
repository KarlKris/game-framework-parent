package com.echo.mongo.core;

import com.echo.common.convert.core.ConversionService;
import com.echo.common.util.ClassUtils;
import com.echo.common.util.ObjectUtils;
import com.echo.common.util.StringUtils;
import com.echo.mongo.CodecRegistryProvider;
import com.echo.mongo.convert.MongoConverter;
import com.echo.mongo.convert.MongoJsonSchemaMapper;
import com.echo.mongo.mapping.MongoMappingContext;
import com.echo.mongo.mapping.MongoPersistentEntity;
import com.echo.mongo.mapping.MongoPersistentProperty;
import com.echo.mongo.query.BasicQuery;
import com.echo.mongo.query.Query;
import com.echo.mongo.query.UpdateDefinition;
import com.echo.mongo.util.BsonUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 集中了在操作真正准备好执行之前所需的常见操作
 * @author: li-yuanwen
 */
public class QueryOperations {

    private static final List<String> DEFAULT_ID_NAMES = Arrays.asList("id", "_id");

    private final MongoConverter converter;

    private final MongoJsonSchemaMapper schemaMapper;

    private final MongoMappingContext mappingContext;

    private final ConversionService conversionService;

    private final CodecRegistryProvider codecRegistryProvider;


    public QueryOperations(MongoConverter converter, MongoMappingContext mappingContext
            , ConversionService conversionService, CodecRegistryProvider codecRegistryProvider) {
        this.converter = converter;
        this.schemaMapper = new MongoJsonSchemaMapper(mappingContext, converter);
        this.mappingContext = mappingContext;
        this.conversionService = conversionService;
        this.codecRegistryProvider = codecRegistryProvider;
    }

    public MongoMappingContext getMappingContext() {
        return mappingContext;
    }

    QueryContext createQueryContext(Query query) {
        return new QueryContext(query);
    }

    InsertContext createInsertContext(Document source) {
        return createInsertContext(MappedDocument.of(source));
    }

    InsertContext createInsertContext(MappedDocument mappedDocument) {
        return new InsertContext(mappedDocument);
    }

    /**
     * Create a new {@link DeleteContext} instance removing all matching documents.
     *
     * @param query must not be {@literal null}.
     * @return new instance of {@link QueryContext}.
     */
    DeleteContext deleteQueryContext(Query query) {
        return new DeleteContext(query);
    }

    /**
     * Create a new {@link UpdateContext} instance affecting multiple documents.
     *
     * @param updateDefinition must not be {@literal null}.
     * @param query            must not be {@literal null}.
     * @param upsert           use {@literal true} to insert diff when no existing document found.
     * @return new instance of {@link UpdateContext}.
     */
    UpdateContext updateContext(UpdateDefinition updateDefinition, Query query, boolean upsert) {
        return new UpdateContext(updateDefinition, query, true, upsert);
    }

    /**
     * Create a new {@link UpdateContext} instance affecting a single document.
     *
     * @param updateDefinition must not be {@literal null}.
     * @param query            must not be {@literal null}.
     * @param upsert           use {@literal true} to insert diff when no existing document found.
     * @return new instance of {@link UpdateContext}.
     */
    UpdateContext updateSingleContext(UpdateDefinition updateDefinition, Query query, boolean upsert) {
        return new UpdateContext(updateDefinition, query, false, upsert);
    }

    /**
     * Create a new {@link UpdateContext} instance affecting a single document.
     *
     * @param updateDefinition must not be {@literal null}.
     * @param query            must not be {@literal null}.
     * @param upsert           use {@literal true} to insert diff when no existing document found.
     * @return new instance of {@link UpdateContext}.
     */
    UpdateContext updateSingleContext(UpdateDefinition updateDefinition, Document query, boolean upsert) {
        return new UpdateContext(updateDefinition, query, false, upsert);
    }

    /**
     * @param replacement the {@link MappedDocument mapped replacement} document.
     * @param upsert      use {@literal true} to insert diff when no existing document found.
     * @return new instance of {@link UpdateContext}.
     */
    UpdateContext replaceSingleContext(MappedDocument replacement, boolean upsert) {
        return new UpdateContext(replacement, upsert);
    }

    /**
     * Maps fields used for sorting to the {@link MongoPersistentEntity}s properties. <br />
     * Also converts properties to their {@code $meta} representation if present.
     *
     * @param sortObject
     * @param entity
     * @return
     * @since 1.6
     */
    public Document getMappedSort(Document sortObject, MongoPersistentEntity entity) {

        if (sortObject == null) {
            throw new IllegalArgumentException("SortObject must not be null");
        }

        if (sortObject.isEmpty()) {
            return BsonUtils.EMPTY_DOCUMENT;
        }

        return mapFieldsToPropertyNames(sortObject, entity);
    }

    private Document mapFieldsToPropertyNames(Document fields, MongoPersistentEntity entity) {

        if (fields.isEmpty()) {
            return BsonUtils.EMPTY_DOCUMENT;
        }

        Document target = new Document();

        BsonUtils.asMap(fields).forEach((k, v) -> {

            Field field = createPropertyField(entity, k, mappingContext);
            if (field.getProperty() != null) {
                return;
            }
            target.put(field.getMappedKey(), v);
        });

        return target;
    }

    /**
     * Returns whether the given {@link Object} is a keyword, i.e. if it's a {@link Document} with a keyword key.
     *
     * @param candidate
     * @return
     */
    protected boolean isNestedKeyword(Object candidate) {

        if (!(candidate instanceof Document)) {
            return false;
        }

        Map<String, Object> map = BsonUtils.asMap((Bson) candidate);

        if (map.size() != 1) {
            return false;
        }

        return isKeyword(map.entrySet().iterator().next().getKey());
    }

    /**
     * Returns whether the given {@link String} is a MongoDB keyword. The default implementation will check against the
     * set of registered keywords.
     *
     * @param candidate
     * @return
     */
    protected boolean isKeyword(String candidate) {
        return candidate.startsWith("$");
    }

    public Document getMappedObject(Bson query, Optional<MongoPersistentEntity> optional) {
        return getMappedObject(query, optional.orElse(null));
    }

    public Document getMappedObject(Bson query, MongoPersistentEntity persistentEntity) {
        if (isNestedKeyword(query)) {
            return getMappedKeyword(new Keyword(query), persistentEntity);
        }

        Document result = new Document();

        for (String key : BsonUtils.asMap(query).keySet()) {

            if (isKeyword(key)) {
                result.putAll(getMappedKeyword(new Keyword(query, key), persistentEntity));
                continue;
            }

            try {

                Field field = createPropertyField(persistentEntity, key, mappingContext);

                if (field.getProperty() != null) {
                    Object theNestedObject = BsonUtils.get(query, key);
                    Object mappedValue = getMappedValue(field, theNestedObject);
                    if (!StringUtils.hasLength(field.getMappedKey())) {
                        if (mappedValue instanceof Document) {
                            result.putAll((Document) mappedValue);
                        } else {
                            result.put(field.getMappedKey(), mappedValue);
                        }
                    } else {
                        result.put(field.getMappedKey(), mappedValue);
                    }
                } else {

                    Map.Entry<String, Object> entry = getMappedObjectForField(field, BsonUtils.get(query, key));

                    result.put(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {

                // in case the object has not already been mapped
                if (!(BsonUtils.get(query, key) instanceof Document)) {
                    throw e;
                }
                result.put(key, BsonUtils.get(query, key));
            }
        }

        return result;
    }

    /**
     * Extracts the mapped object value for given field out of rawValue taking nested {@link Keyword}s into account
     *
     * @param field
     * @param rawValue
     * @return
     */
    protected Map.Entry<String, Object> getMappedObjectForField(Field field, Object rawValue) {

        String key = field.getMappedKey();
        Object value;

        if (isNestedKeyword(rawValue) && !field.isIdField()) {
            Keyword keyword = new Keyword((Document) rawValue);
            value = getMappedKeyword(field, keyword);
        } else {
            value = getMappedValue(field, rawValue);
        }

        return createMapEntry(key, value);
    }

    /**
     * Returns the mapped value for the given source object assuming it's a value for the given
     * {@link MongoPersistentProperty}.
     *
     * @param documentField the key the value will be bound to eventually
     * @param sourceValue the source object to be mapped
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Object getMappedValue(Field documentField, Object sourceValue) {

        Object value = applyFieldTargetTypeHintToValue(documentField, sourceValue);

        if (documentField.isIdField()) {

            if (isDBObject(value)) {
                DBObject valueDbo = (DBObject) value;
                Document resultDbo = new Document(valueDbo.toMap());

                if (valueDbo.containsField("$in") || valueDbo.containsField("$nin")) {
                    String inKey = valueDbo.containsField("$in") ? "$in" : "$nin";
                    List<Object> ids = new ArrayList<>();
                    for (Object id : (Iterable<?>) valueDbo.get(inKey)) {
                        ids.add(convertId(id, getIdTypeForField(documentField)));
                    }
                    resultDbo.put(inKey, ids);
                } else if (valueDbo.containsField("$ne")) {
                    resultDbo.put("$ne", convertId(valueDbo.get("$ne"), getIdTypeForField(documentField)));
                } else {
                    return getMappedObject(resultDbo, Optional.empty());
                }
                return resultDbo;
            }

            else if (isDocument(value)) {
                Document valueDbo = (Document) value;
                Document resultDbo = new Document(valueDbo);

                if (valueDbo.containsKey("$in") || valueDbo.containsKey("$nin")) {
                    String inKey = valueDbo.containsKey("$in") ? "$in" : "$nin";
                    List<Object> ids = new ArrayList<>();
                    for (Object id : (Iterable<?>) valueDbo.get(inKey)) {
                        ids.add(convertId(id, getIdTypeForField(documentField)));
                    }
                    resultDbo.put(inKey, ids);
                } else if (valueDbo.containsKey("$ne")) {
                    resultDbo.put("$ne", convertId(valueDbo.get("$ne"), getIdTypeForField(documentField)));
                } else {
                    return getMappedObject(resultDbo, Optional.empty());
                }
                return resultDbo;

            } else {
                return convertId(value, getIdTypeForField(documentField));
            }
        }

        if (value == null) {
            return null;
        }

        if (isNestedKeyword(value)) {
            return getMappedKeyword(new Keyword((Bson) value), documentField.getPropertyEntity());
        }

        return convertSimpleOrDocument(value, documentField.getPropertyEntity());
    }

    /**
     * Converts the given raw id value into either {@link ObjectId} or {@link Class targetType}.
     *
     * @param id can be {@literal null}.
     * @param targetType
     * @return the converted {@literal id} or {@literal null} if the source was already {@literal null}.
     * @since 2.2
     */
    public Object convertId(Object id, Class<?> targetType) {
        return converter.convertId(id, targetType);
    }


    private boolean isIdField(Field documentField) {
        return documentField.getProperty() != null && documentField.getProperty().isIdProperty();
    }

    private Class<?> getIdTypeForField(Field documentField) {
        return isIdField(documentField) ? documentField.getProperty().getFieldType() : ObjectId.class;
    }

    /**
     * Convert the given field value into its desired
     * {@link com.echo.mongo.mapping.anno.Field#targetType() target type} before applying further
     * conversions. In case of a {@link Collection} (used eg. for {@code $in} queries) the individual values will be
     * converted one by one.
     *
     * @param documentField the field and its meta data
     * @param value the actual value. Can be {@literal null}.
     * @return the potentially converted target value.
     */
    private Object applyFieldTargetTypeHintToValue(Field documentField, Object value) {

        if (value == null || documentField.getProperty() == null || !documentField.getProperty().hasExplicitWriteTarget()
                || value instanceof Document || value instanceof DBObject) {
            return value;
        }

        if (!conversionService.canConvert(value.getClass(), documentField.getProperty().getFieldType())) {
            return value;
        }

        if (value instanceof Collection<?>) {
            Collection<?> source = (Collection<?>) value;

            Collection<Object> converted = new ArrayList<>(source.size());

            for (Object o : source) {
                converted.add(conversionService.convert(o, documentField.getProperty().getFieldType()));
            }

            return converted;
        }

        return conversionService.convert(value, documentField.getProperty().getFieldType());
    }

    /**
     * Returns the given {@link Document} representing a keyword by mapping the keyword's value.
     *
     * @param keyword the {@link Document} representing a keyword (e.g. {@code $ne : … } )
     * @param entity
     * @return
     */
    protected Document getMappedKeyword(Keyword keyword, MongoPersistentEntity entity) {

        // $or/$nor
        if (keyword.isOrOrNor() || (keyword.hasIterableValue() && !keyword.isGeometry())) {

            Iterable<?> conditions = keyword.getValue();
            List<Object> newConditions = conditions instanceof Collection<?>
                    ? new ArrayList<>(((Collection<?>) conditions).size())
                    : new ArrayList<>();

            for (Object condition : conditions) {
                newConditions.add(isDocument(condition) ? getMappedObject((Document) condition, entity)
                        : convertSimpleOrDocument(condition, entity));
            }

            return new Document(keyword.getKey(), newConditions);
        }

        if (keyword.isJsonSchema()) {
            return schemaMapper.mapSchema(new Document(keyword.getKey(), keyword.getValue()),
                    entity != null ? entity.getType() : Object.class);
        }

        return new Document(keyword.getKey(), convertSimpleOrDocument(keyword.getValue(), entity));
    }


    /**
     * Returns the mapped keyword considered defining a criteria for the given property.
     *
     * @param property
     * @param keyword
     * @return
     */
    protected Document getMappedKeyword(Field property, Keyword keyword) {

        Object value = keyword.getValue();

        Object convertedValue = getMappedValue(property.with(keyword.getKey()), value);

        return new Document(keyword.key, convertedValue);
    }


    /**
     * Retriggers mapping if the given source is a {@link Document} or simply invokes the
     *
     * @param source
     * @param entity
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Object convertSimpleOrDocument(Object source, MongoPersistentEntity entity) {

        if (source instanceof List) {
            return delegateConvertToMongoType(source, entity);
        }

        if (isDocument(source)) {
            return getMappedObject((Document) source, entity);
        }

        if (isDBObject(source)) {
            return getMappedObject((BasicDBObject) source, entity);
        }

        if (source instanceof BsonValue) {
            return source;
        }

        if (source instanceof Map) {

            Map<String, Object> map = new LinkedHashMap<>();

            ((Map<String, Object>) source).entrySet().forEach(it -> {

                String key = ObjectUtils.nullSafeToString(converter.convertToMongoType(it.getKey()));

                if (it.getValue() instanceof Document) {
                    map.put(key, getMappedObject((Document) it.getValue(), entity));
                } else {
                    map.put(key, delegateConvertToMongoType(it.getValue(), entity));
                }
            });

            return map;
        }

        return delegateConvertToMongoType(source, entity);
    }

    /**
     * Converts the given source Object to a mongo type with the type information of the original source type omitted.
     * Subclasses may overwrite this method to retain the type information of the source type on the resulting mongo type.
     *
     * @param source
     * @param entity
     * @return the converted mongo type or null if source is null
     */
    protected Object delegateConvertToMongoType(Object source, MongoPersistentEntity entity) {
        return converter.convertToMongoType(source, entity == null ? null : entity.getTypeDescriptor());
    }

    protected final boolean isDocument(Object value) {
        return value instanceof Document;
    }

    /**
     * Checks whether the given value is a {@link DBObject}.
     *
     * @param value can be {@literal null}.
     * @return
     */
    protected final boolean isDBObject(Object value) {
        return value instanceof DBObject;
    }

    /**
     * Creates a new {@link Map.Entry} for the given {@link Field} with the given value.
     *
     * @param field must not be {@literal null}.
     * @param value can be {@literal null}.
     * @return
     */
    protected final Map.Entry<String, Object> createMapEntry(Field field, Object value) {
        return createMapEntry(field.getMappedKey(), value);
    }

    /**
     * Creates a new {@link Map.Entry} with the given key and value.
     *
     * @param key must not be {@literal null} or empty.
     * @param value can be {@literal null}.
     * @return
     */
    private Map.Entry<String, Object> createMapEntry(String key, Object value) {

        if (!StringUtils.hasLength(key)) {
            throw new IllegalArgumentException("Key must not be null or empty");
        }
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    protected Field createPropertyField(MongoPersistentEntity entity, String key,
                                        MongoMappingContext mappingContext) {

        if (entity == null) {
            return new Field(key);
        }

        if (Field.ID_KEY.equals(key)) {
            return new MetadataBackedField(key, entity, mappingContext, entity.getIdProperty());
        }

        return new MetadataBackedField(key, entity, mappingContext);
    }

    class QueryContext {

        private final Query query;

        private QueryContext(Query query) {
            this.query = query;
        }

        public Query getQuery() {
            return query;
        }

        Document getMappedQuery(MongoPersistentEntity persistentEntity) {
            return getMappedObject(query.getQueryObject(), persistentEntity);
        }

        Document getMappedFields(MongoPersistentEntity persistentEntity) {
            return evaluateFields(persistentEntity);
        }

        private Document evaluateFields(MongoPersistentEntity persistentEntity) {
            Document fields = query.getFieldsObject();

            if (fields.isEmpty()) {
                return BsonUtils.EMPTY_DOCUMENT;
            }

            Document evaluated = new Document();
            evaluated.putAll(fields);
            return evaluated;
        }

    }

    /**
     * A {@link QueryContext} that encapsulates common tasks required when running {@literal delete} queries.
     *
     * @author Christoph Strobl
     */
    class DeleteContext extends QueryContext {

        /**
         * Crate a new {@link DeleteContext} instance.
         *
         * @param query can be {@literal null}.
         */
        DeleteContext(Query query) {
            super(query);
        }

        /**
         * Get the {@link DeleteOptions} applicable for the {@link Query}.
         *
         * @param domainType must not be {@literal null}.
         * @return never {@literal null}.
         */
        DeleteOptions getDeleteOptions(Class<?> domainType) {
            return getDeleteOptions(domainType, null);
        }

        /**
         * Get the {@link DeleteOptions} applicable for the {@link Query}.
         *
         * @param domainType can be {@literal null}.
         * @param callback   a callback to modify the generated options. Can be {@literal null}.
         * @return
         */
        DeleteOptions getDeleteOptions(Class<?> domainType, Consumer<DeleteOptions> callback) {
            DeleteOptions options = new DeleteOptions();
            if (callback != null) {
                callback.accept(options);
            }
            return options;
        }
    }


    /**
     * A {@link QueryContext} that encapsulates common tasks required when running {@literal updates}.
     */
    class UpdateContext extends QueryContext {

        private final boolean multi;
        private final boolean upsert;
        private final UpdateDefinition update;
        private final MappedDocument mappedDocument;

        /**
         * Create a new {@link UpdateContext} instance.
         *
         * @param update must not be {@literal null}.
         * @param query  must not be {@literal null}.
         * @param multi  use {@literal true} to update all matching documents.
         * @param upsert use {@literal true} to insert a new document if none match.
         */
        UpdateContext(UpdateDefinition update, Document query, boolean multi, boolean upsert) {
            this(update, new BasicQuery(query), multi, upsert);
        }

        /**
         * Create a new {@link UpdateContext} instance.
         *
         * @param update must not be {@literal null}.
         * @param query  can be {@literal null}.
         * @param multi  use {@literal true} to update all matching documents.
         * @param upsert use {@literal true} to insert a new document if none match.
         */
        UpdateContext(UpdateDefinition update, Query query, boolean multi, boolean upsert) {

            super(query);

            this.multi = multi;
            this.upsert = upsert;
            this.update = update;
            this.mappedDocument = null;
        }

        UpdateContext(MappedDocument update, boolean upsert) {

            super(new BasicQuery(BsonUtils.asDocument(update.getIdFilter())));
            this.multi = false;
            this.upsert = upsert;
            this.mappedDocument = update;
            this.update = null;
        }

        /**
         * Get the {@link UpdateOptions} applicable for the {@link Query}.
         *
         * @param domainType must not be {@literal null}.
         * @return never {@literal null}.
         */
        UpdateOptions getUpdateOptions(Class<?> domainType) {
            return getUpdateOptions(domainType, null);
        }

        /**
         * Get the {@link UpdateOptions} applicable for the {@link Query}.
         *
         * @param domainType can be {@literal null}.
         * @param callback   a callback to modify the generated options. Can be {@literal null}.
         * @return
         */
        UpdateOptions getUpdateOptions(Class<?> domainType, Consumer<UpdateOptions> callback) {

            UpdateOptions options = new UpdateOptions();
            options.upsert(upsert);

            if (update != null && update.hasArrayFilters()) {
                options
                        .arrayFilters(update.getArrayFilters().stream().map(UpdateDefinition.ArrayFilter::asDocument).collect(Collectors.toList()));
            }

            HintFunction.from(getQuery().getHint()).ifPresent(codecRegistryProvider, options::hintString, options::hint);

            if (callback != null) {
                callback.accept(options);
            }

            return options;
        }

        /**
         * Get the {@link ReplaceOptions} applicable for the {@link Query}.
         *
         * @param domainType must not be {@literal null}.
         * @return never {@literal null}.
         */
        ReplaceOptions getReplaceOptions(Class<?> domainType) {
            return getReplaceOptions(domainType, null);
        }

        /**
         * Get the {@link ReplaceOptions} applicable for the {@link Query}.
         *
         * @param domainType can be {@literal null}.
         * @param callback   a callback to modify the generated options. Can be {@literal null}.
         * @return
         */
        ReplaceOptions getReplaceOptions(Class<?> domainType, Consumer<ReplaceOptions> callback) {

            UpdateOptions updateOptions = getUpdateOptions(domainType);

            ReplaceOptions options = new ReplaceOptions();
            options.collation(updateOptions.getCollation());
            options.upsert(updateOptions.isUpsert());

            if (callback != null) {
                callback.accept(options);
            }

            return options;
        }

        @Override
        Document getMappedQuery(MongoPersistentEntity domainType) {

            Document mappedQuery = super.getMappedQuery(domainType);

            if (multi && update.isIsolated() && !mappedQuery.containsKey("$isolated")) {
                mappedQuery.put("$isolated", 1);
            }

            return mappedQuery;
        }

//        Document applyShardKey(MongoPersistentEntity domainType, Document filter, Document existing) {
//
//            Document shardKeySource = existing != null ? existing
//                    : mappedDocument != null ? mappedDocument.getDocument() : getMappedUpdate(domainType);
//
//            Document filterWithShardKey = new Document(filter);
//            getMappedShardKeyFields(domainType)
//                    .forEach(key -> filterWithShardKey.putIfAbsent(key, BsonUtils.resolveValue((Bson) shardKeySource, key)));
//
//            return filterWithShardKey;
//        }

//        boolean requiresShardKey(Document filter, MongoPersistentEntity domainType) {
//
//            return !multi && domainType != null && domainType.isSharded() && !shardedById(domainType)
//                    && !filter.keySet().containsAll(getMappedShardKeyFields(domainType));
//        }

//        /**
//         * @return {@literal true} if the {@link MongoPersistentEntity#getShardKey() shard key} is the entities
//         *         {@literal id} property.
//         * @since 3.0
//         */
//        private boolean shardedById(MongoPersistentEntity domainType) {
//
//            ShardKey shardKey = domainType.getShardKey();
//            if (shardKey.size() != 1) {
//                return false;
//            }
//
//            String key = shardKey.getPropertyNames().iterator().next();
//            if ("_id".equals(key)) {
//                return true;
//            }
//
//            MongoPersistentProperty idProperty = domainType.getIdProperty();
//            return idProperty != null && idProperty.getName().equals(key);
//        }
//
//        Set<String> getMappedShardKeyFields(MongoPersistentEntity entity) {
//            return getMappedShardKey(entity).keySet();
//        }
//
//        Document getMappedShardKey(MongoPersistentEntity entity) {
//            return mappedShardKey.computeIfAbsent(entity.getType(),
//                    key -> queryMapper.getMappedFields(entity.getShardKey().getDocument(), entity));
//        }


        /**
         * Get the already mapped update {@link Document}.
         *
         * @param entity
         * @return
         */
        Document getMappedUpdate(MongoPersistentEntity entity) {

            if (update != null) {
                return update instanceof MappedDocument.MappedUpdate ? update.getUpdateObject()
                        : getMappedObject(update.getUpdateObject(), entity);
            }
            return mappedDocument.getDocument();
        }

        Document getMappedObject(Bson bson, MongoPersistentEntity entity) {
            Document document = QueryOperations.this.getMappedObject(bson, entity);
            boolean hasOperators = false;
            boolean hasFields = false;

            Document set = null;
            for (String s : document.keySet()) {
                if (s.startsWith("$")) {

                    if (s.equals("$set")) {
                        set = document.get(s, Document.class);
                    }
                    hasOperators = true;
                } else {
                    hasFields = true;
                }
            }

            if (hasOperators && hasFields) {

                Document updateObject = new Document();
                Document fieldsToSet = set == null ? new Document() : set;

                for (String s : document.keySet()) {
                    if (s.startsWith("$")) {
                        updateObject.put(s, document.get(s));
                    } else {
                        fieldsToSet.put(s, document.get(s));
                    }
                }
                updateObject.put("$set", fieldsToSet);

                return updateObject;
            }
            return document;
        }


        /**
         * @return {@literal true} if all matching documents should be updated.
         */
        boolean isMulti() {
            return multi;
        }
    }

    /**
     * {@link InsertContext} encapsulates common tasks required to interact with {@link Document} to be inserted.
     *
     * @since 3.4.3
     */
    class InsertContext {

        private final MappedDocument source;

        private InsertContext(MappedDocument source) {
            this.source = source;
        }

        /**
         * Prepare the {@literal _id} field. May generate a new {@literal id} value and convert it to the id properties
         * {@link MongoPersistentProperty#getFieldType() target type}.
         *
         * @param type must not be {@literal null}.
         * @param <T>
         * @return the {@link MappedDocument} containing the changes.
         * @see #prepareId(MongoPersistentEntity)
         */
        <T> MappedDocument prepareId(Class<T> type) {
            return prepareId(mappingContext.getPersistentEntity(type));
        }

        /**
         * Prepare the {@literal _id} field. May generate a new {@literal id} value and convert it to the id properties
         * {@link MongoPersistentProperty#getFieldType() target type}.
         *
         * @param entity can be {@literal null}.
         * @param <T>
         * @return the {@link MappedDocument} containing the changes.
         */
        <T> MappedDocument prepareId(MongoPersistentEntity entity) {

            if (entity == null || source.hasId()) {
                return source;
            }

            MongoPersistentProperty idProperty = entity.getIdProperty();
            if (idProperty != null
                    && (idProperty.hasExplicitWriteTarget())) {
                if (!ClassUtils.isAssignable(ObjectId.class, idProperty.getFieldType())) {
                    source.updateId(convertId(new ObjectId(), idProperty.getFieldType()));
                }
            }
            return source;
        }
    }

    /**
     * Value object to capture a query keyword representation.
     *
     * @author Oliver Gierke
     * @author Christoph Strobl
     */
    static class Keyword {

        private static final Set<String> NON_DBREF_CONVERTING_KEYWORDS = new HashSet<>(
                Arrays.asList("$", "$size", "$slice", "$gt", "$lt"));

        private final String key;
        private final Object value;

        public Keyword(Bson source, String key) {
            this.key = key;
            this.value = BsonUtils.get(source, key);
        }

        public Keyword(Bson bson) {

            Map<String, Object> map = BsonUtils.asMap(bson);
            if (map.size() != 1) {
                throw new IllegalArgumentException("Can only use a single value Document");
            }

            Set<Map.Entry<String, Object>> entries = map.entrySet();
            Map.Entry<String, Object> entry = entries.iterator().next();

            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        /**
         * Returns whether the current keyword is the {@code $exists} keyword.
         *
         * @return
         */
        public boolean isExists() {
            return "$exists".equalsIgnoreCase(key);
        }

        public boolean isOrOrNor() {
            return key.equalsIgnoreCase("$or") || key.equalsIgnoreCase("$nor");
        }

        /**
         * Returns whether the current keyword is the {@code $geometry} keyword.
         *
         * @return
         * @since 1.8
         */
        public boolean isGeometry() {
            return "$geometry".equalsIgnoreCase(key);
        }

        public boolean hasIterableValue() {
            return value instanceof Iterable;
        }

        public String getKey() {
            return key;
        }

        @SuppressWarnings("unchecked")
        public <T> T getValue() {
            return (T) value;
        }

        /**
         * @return {@literal true} if key may hold a DbRef.
         * @since 2.1.4
         */
        public boolean mayHoldDbRef() {
            return !NON_DBREF_CONVERTING_KEYWORDS.contains(key);
        }

        /**
         * Returns whether the current keyword indicates a {@literal $jsonSchema} object.
         *
         * @return {@literal true} if {@code key} equals {@literal $jsonSchema}.
         * @since 2.1
         */
        public boolean isJsonSchema() {
            return "$jsonSchema".equalsIgnoreCase(key);
        }
    }


    /**
     * Value object to represent a field and its meta-information.
     *
     * @author Oliver Gierke
     */
    protected static class Field {

        protected static final Pattern POSITIONAL_OPERATOR = Pattern.compile("\\$\\[.*\\]");

        private static final String ID_KEY = "_id";

        protected final String name;

        /**
         * Creates a new {@link Field} without meta-information but the given name.
         *
         * @param name must not be {@literal null} or empty.
         */
        public Field(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name must not be null");
            }
            this.name = name;
        }

        /**
         * Returns a new {@link Field} with the given name.
         *
         * @param name must not be {@literal null} or empty.
         * @return
         */
        public Field with(String name) {
            return new Field(name);
        }

        /**
         * Returns whether the current field is the id field.
         *
         * @return
         */
        public boolean isIdField() {
            return ID_KEY.equals(name);
        }

        /**
         * Returns the underlying {@link MongoPersistentProperty} backing the field. For path traversals this will be the
         * property that represents the value to handle. This means it'll be the leaf property for plain paths or the
         * association property in case we refer to an association somewhere in the path.
         *
         * @return can be {@literal null}.
         */
        public MongoPersistentProperty getProperty() {
            return null;
        }

        /**
         * Returns the {@link MongoPersistentEntity} that field is conatined in.
         *
         * @return can be {@literal null}.
         */
        public MongoPersistentEntity getPropertyEntity() {
            return null;
        }


        MongoPersistentEntity getEntity() {
            return null;
        }

        /**
         * Returns the key to be used in the mapped document eventually.
         *
         * @return
         */
        public String getMappedKey() {
            return isIdField() ? ID_KEY : name;
        }


        /**
         * Returns whether the field references a {@link java.util.Map}.
         *
         * @return {@literal true} if property information is available and references a {@link java.util.Map}.
         * @see MongoPersistentProperty#isMap()
         */
        public boolean isMap() {
            return getProperty() != null && getProperty().isMap();
        }

        public Class<?> getFieldType() {
            return Object.class;
        }
    }

    /**
     * Extension of {@link Field} to be backed with mapping metadata.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    protected static class MetadataBackedField extends Field {

        private static final Pattern POSITIONAL_PARAMETER_PATTERN = Pattern.compile("\\.\\$(\\[.*?\\])?");
        private static final Pattern NUMERIC_SEGMENT = Pattern.compile("\\d+");
        private static final String INVALID_ASSOCIATION_REFERENCE = "Invalid path reference %s; Associations can only be pointed to directly or via their id property";

        private final MongoPersistentEntity entity;
        private final MongoPersistentProperty property;

        private final MongoMappingContext mappingContext;

        /**
         * Creates a new {@link MetadataBackedField} with the given name, {@link MongoPersistentEntity} and
         * {@link MongoMappingContext}.
         *
         * @param name must not be {@literal null} or empty.
         * @param entity must not be {@literal null}.
         * @param mappingContext must not be {@literal null}.
         */
        public MetadataBackedField(String name, MongoPersistentEntity entity, MongoMappingContext mappingContext) {
            this(name, entity, mappingContext, null);
        }

        /**
         * Creates a new {@link MetadataBackedField} with the given name, {@link MongoPersistentEntity} and
         * {@link MongoMappingContext} with the given {@link MongoPersistentProperty}.
         *
         * @param name must not be {@literal null} or empty.
         * @param entity must not be {@literal null}.
         * @param mappingContext must not be {@literal null}.
         * @param property may be {@literal null}.
         */
        public MetadataBackedField(String name, MongoPersistentEntity entity, MongoMappingContext mappingContext, MongoPersistentProperty property) {

            super(name);

            if (entity == null) {
                throw new IllegalArgumentException("MongoPersistentEntity must not be null");
            }

            this.entity = entity;
            this.property = property;
            this.mappingContext = mappingContext;
        }

        @Override
        public MetadataBackedField with(String name) {
            return new MetadataBackedField(name, entity, mappingContext, property);
        }

        @Override
        public boolean isIdField() {

            if (property != null) {
                return property.isIdProperty();
            }

            MongoPersistentProperty idProperty = entity.getIdProperty();

            if (idProperty != null) {
                return name.equals(idProperty.getFieldName());
            }

            return DEFAULT_ID_NAMES.contains(name);
        }

        @Override
        public MongoPersistentProperty getProperty() {
            return property;
        }

        @Override
        public MongoPersistentEntity getPropertyEntity() {
            MongoPersistentProperty property = getProperty();
            return property == null ? null : mappingContext.getPersistentEntity(property.getActualType());
        }

        @Override
        public MongoPersistentEntity getEntity() {
            return entity;
        }


        @Override
        public Class<?> getFieldType() {
            return property.getFieldType();
        }

        @Override
        public String getMappedKey() {
            return name;
        }

    }


    /**
     * Returns {@literal true} if the given {@link Document} is an update object that uses update operators.
     *
     * @param updateObj can be {@literal null}.
     * @return {@literal true} if the given {@link Document} is an update object.
     */
    public static boolean isUpdateObject(Document updateObj) {

        if (updateObj == null) {
            return false;
        }

        for (String s : updateObj.keySet()) {
            if (s.startsWith("$")) {
                return true;
            }
        }

        return false;
    }

}
