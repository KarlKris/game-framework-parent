package com.echo.mongo.convert;

import cn.hutool.core.lang.Assert;
import com.echo.common.convert.CustomConversions;
import com.echo.common.convert.core.ConfigurableConversionService;
import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.*;
import com.echo.mongo.CodecRegistryProvider;
import com.echo.mongo.excetion.MappingException;
import com.echo.mongo.mapping.*;
import com.echo.mongo.util.BsonUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.json.JsonReader;
import org.bson.types.ObjectId;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * mongo 类型转换
 * @author: li-yuanwen
 */
public class GenericMongoConverter implements MongoConverter {

    private static final String INCOMPATIBLE_TYPES = "Cannot convert %1$s of type %2$s into an instance of %3$s; Implement a custom Converter<%2$s, %3$s> and register it with the CustomConversions;";
    private static final String INVALID_TYPE_TO_READ = "Expected to read Document %s into type %s but didn't find a PersistentEntity for the latter";

    public static final String TYPE_KEY = "_class";
    private static final TypeDescriptor LIST_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(List.class);
    private static final TypeDescriptor MAP_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(Map.class);
    private static final TypeDescriptor BSON_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(Bson.class);

    private final ConfigurableConversionService conversionService;
    private CustomConversions conversions = new MongoCustomConversions();

    /**
     * 用来替换keu中的{.}字符
     **/
    protected String mapKeyDotReplacement = null;

    /**
     * 存储_class字段开关
     **/
    private boolean saveExtraClass = true;

    private final MongoMappingContext mappingContext;
    protected CodecRegistryProvider codecRegistryProvider;

    private final Map<Alias, Optional<TypeDescriptor>> typeCache = new ConcurrentHashMap<>();

    public GenericMongoConverter(ConfigurableConversionService conversionService, MongoMappingContext mappingContext) {
        this.conversionService = conversionService;
        this.mappingContext = mappingContext;
        initializeConverters();
    }

    public MongoMappingContext getMappingContext() {
        return mappingContext;
    }


    /**
     * Registers the given custom conversions with the converter.
     *
     * @param conversions must not be {@literal null}.
     */
    public void setCustomConversions(CustomConversions conversions) {

        Assert.notNull(conversions, "Conversions must not be null");
        this.conversions = conversions;
    }

    /**
     * Configure a {@link CodecRegistryProvider} that provides native MongoDB {@link org.bson.codecs.Codec codecs} for
     * reading values.
     *
     * @param codecRegistryProvider can be {@literal null}.
     */
    public void setCodecRegistryProvider(CodecRegistryProvider codecRegistryProvider) {
        this.codecRegistryProvider = codecRegistryProvider;
    }

    /**
     * Configure the characters dots potentially contained in a {@link Map} shall be replaced with. By default we don't do
     * any translation but rather reject a {@link Map} with keys containing dots causing the conversion for the entire
     * object to fail. If further customization of the translation is needed, have a look at
     * {@link #potentiallyEscapeMapKey(String)} as well as {@link #potentiallyUnescapeMapKey(String)}.
     * <p>
     * {@code mapKeyDotReplacement} is used as-is during replacement operations without further processing (i.e. regex or
     * normalization).
     *
     * @param mapKeyDotReplacement the mapKeyDotReplacement to set. Can be {@literal null}.
     */
    public void setMapKeyDotReplacement(String mapKeyDotReplacement) {
        this.mapKeyDotReplacement = mapKeyDotReplacement;
    }

    public void setSaveExtraClass(boolean saveExtraClass) {
        this.saveExtraClass = saveExtraClass;
    }

    /**
     * Registers additional converters that will be available when using the {@link ConversionService} directly (e.g. for
     * id conversion). These converters are not custom conversions as they'd introduce unwanted conversions (e.g.
     * ObjectId-to-String).
     */
    private void initializeConverters() {
        conversionService.addConverter(MongoConverters.ObjectIdToStringConverter.INSTANCE);
        conversionService.addConverter(MongoConverters.StringToObjectIdConverter.INSTANCE);

        if (!conversionService.canConvert(ObjectId.class, BigInteger.class)) {
            conversionService.addConverter(MongoConverters.ObjectIdToBigIntegerConverter.INSTANCE);
        }

        if (!conversionService.canConvert(BigInteger.class, ObjectId.class)) {
            conversionService.addConverter(MongoConverters.BigIntegerToObjectIdConverter.INSTANCE);
        }

        if (!conversionService.canConvert(Date.class, Long.class)) {
            conversionService.addConverter(MongoConverters.DateToLongConverter.INSTANCE);
        }

        if (!conversionService.canConvert(Long.class, Date.class)) {
            conversionService.addConverter(MongoConverters.LongToDateConverter.INSTANCE);
        }

        if (!conversionService.canConvert(ObjectId.class, Date.class)) {

            conversionService.addConverter(MongoConverters.ObjectIdToDateConverter.INSTANCE);
        }

        conversionService.addConverter(MongoConverters.CodeToStringConverter.INSTANCE);
        conversions.registerConvertersIn(conversionService);
    }

    @Override
    public <T> T read(Class<T> type, Bson source) {
        DefaultConversionContext context = new DefaultConversionContext(this, conversions, this::readDocument
                , this::readCollectionOrArray, this::readMap, this::getPotentiallyConvertedSimpleRead);
        return readDocument(context, source, TypeDescriptor.valueOf(type));
    }


    @SuppressWarnings("unchecked")
    protected <T> T readDocument(ConversionContext context, Bson bson, TypeDescriptor descriptor) {
        Document document = bson instanceof BasicDBObject ? new Document((BasicDBObject) bson) : (Document) bson;
        TypeDescriptor typeDescriptor = readType(document, descriptor);
        Class<? extends T> rawType = (Class<? extends T>) typeDescriptor.getType();

        if (conversions.hasCustomReadTarget(bson.getClass(), rawType)) {
            Class<? extends T> fallback = (Class<? extends T>) descriptor.getType();
            return doConvert(bson, rawType, fallback);
        }

        if (Document.class.isAssignableFrom(rawType)) {
            return (T) bson;
        }

        if (DBObject.class.isAssignableFrom(rawType)) {
            if (bson instanceof DBObject) {
                return (T) bson;
            }
            return (T) new BasicDBObject((Document) bson);
        }

        if (typeDescriptor.isMap()) {
            return context.convert(bson, typeDescriptor);
        }

        if (BSON_TYPE_DESCRIPTOR.isAssignableFrom(descriptor)) {
            return (T) bson;
        }

        MongoPersistentEntity entity = mappingContext.getPersistentEntity(typeDescriptor);
        if (entity == null) {
            if (codecRegistryProvider != null) {

                Optional<? extends Codec<? extends T>> codec = codecRegistryProvider.getCodecFor(rawType);
                if (codec.isPresent()) {
                    return codec.get().decode(new JsonReader(document.toJson()), DecoderContext.builder().build());
                }
            }

            throw new MappingException(String.format(INVALID_TYPE_TO_READ, document, rawType));
        }

        return read(context, entity, document);
    }


    private TypeDescriptor readType(Bson source, TypeDescriptor defaultDescriptor) {
        TypeDescriptor typeDescriptor = readType(source);
        if (typeDescriptor == null) {
            typeDescriptor = getFallbackTypeFor(source);
        }
        if (typeDescriptor == null) {
            return defaultDescriptor;
        }

        Class<?> documentsTargetType = typeDescriptor.getType();
        Class<?> rawType = defaultDescriptor.getType();

        boolean isMoreConcreteCustomType = (rawType == null)
                || (rawType.isAssignableFrom(documentsTargetType) && !rawType.equals(documentsTargetType));

        if (!isMoreConcreteCustomType) {
            return defaultDescriptor;
        }

        return typeDescriptor;
    }


    private TypeDescriptor readType(Bson source) {
        Alias alias = readAliasFrom(source);
        Optional<TypeDescriptor> optional = typeCache.get(alias);
        if (optional == null) {
            optional = typeCache.computeIfAbsent(alias, new Function<Alias, Optional<TypeDescriptor>>() {
                @Override
                public Optional<TypeDescriptor> apply(Alias alias) {
                    String stringAlias = alias.mapTyped(String.class);

                    if (stringAlias != null) {
                        return loadClass(stringAlias);
                    }
                    return Optional.empty();
                }
            });
        }
        return optional.orElse(null);
    }

    @SuppressWarnings("unchecked")
    private <T> T read(ConversionContext context, MongoPersistentEntity entity, Document bson) {
        DocumentAccessor documentAccessor = new DocumentAccessor(bson);
        T instance = (T) ObjectUtils.newInstance(entity.getType());
        return populateProperties(context, entity, documentAccessor, instance);
    }

    private <T> T populateProperties(ConversionContext context, MongoPersistentEntity entity,
                                     DocumentAccessor documentAccessor, T instance) {
        PersistentPropertyAccessor<T> accessor = new InstanceWrapper<>(instance);
        PropertyValueProvider provider = new MongoDbPropertyValueProvider(context, documentAccessor);
        for (MongoPersistentProperty prop : entity) {
            if (!documentAccessor.hasValue(prop)) {
                continue;
            }
            accessor.setProperty(prop, provider.getPropertyValue(prop));
        }
        return accessor.getBean();
    }


    protected TypeDescriptor getFallbackTypeFor(Bson source) {
        return source instanceof BasicDBList ? LIST_TYPE_DESCRIPTOR : MAP_TYPE_DESCRIPTOR;
    }

    private Optional<TypeDescriptor> loadClass(String typeName) {

        try {
            return Optional.of(TypeDescriptor.valueOf(ClassUtils.forName(typeName, null)));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private Alias readAliasFrom(Bson source) {

        if (source instanceof List) {
            return Alias.NONE;
        }

        if (source instanceof Document) {
            Document document = (Document) source;
            return Alias.ofNullable(document.get(TYPE_KEY));
        } else if (source instanceof DBObject) {
            DBObject dbObject = (DBObject) source;
            return Alias.ofNullable(dbObject.get(TYPE_KEY));
        }

        throw new IllegalArgumentException("Cannot read alias from " + source.getClass());
    }

    /**
     * Returns the alias to be used for the given {@link TypeDescriptor}.
     *
     * @param info must not be {@literal null}
     * @return the alias for the given {@link TypeDescriptor} or {@literal null} of none was found or all mappers
     * returned {@literal null}.
     */
    protected final Alias getAliasFor(TypeDescriptor info) {

        if (info == null) {
            throw new IllegalArgumentException("TypeInformation must not be null");
        }

        Alias alias = createAliasFor(info);
        if (alias.isPresent()) {
            return alias;
        }

        return Alias.NONE;
    }

    public Alias createAliasFor(TypeDescriptor type) {
        return Alias.of(type.getType().getName());
    }

    public void writeType(TypeDescriptor typeDescriptor, Bson sink) {

        if (typeDescriptor == null) {
            throw new IllegalArgumentException("TypeInformation must not be null");
        }

        Alias alias = getAliasFor(typeDescriptor);
        if (alias.isPresent()) {
            writeTypeTo(sink, alias.getValue());
        }
    }

    private void writeTypeTo(Bson sink, Object alias) {
        if (!saveExtraClass) {
            return;
        }
        if (sink instanceof Document) {
            ((Document) sink).put(TYPE_KEY, alias);
        } else if (sink instanceof DBObject) {
            ((DBObject) sink).put(TYPE_KEY, alias);
        }
    }

    private <T extends Object> T doConvert(Object value, Class<? extends T> target) {
        return doConvert(value, target, null);
    }

    private <T extends Object> T doConvert(Object value, Class<? extends T> target,
                                           Class<? extends T> fallback) {

        if (conversionService.canConvert(value.getClass(), target) || fallback == null) {
            return conversionService.convert(value, target);
        }
        return conversionService.convert(value, fallback);
    }


    @Override
    public void write(Object source, Bson sink) {
        Class<?> entityType = ClassUtils.getUserClass(source.getClass());
        TypeDescriptor type = TypeDescriptor.valueOf(entityType);

        writeInternal(source, sink, type);
        BsonUtils.removeNullId(sink);
        if (requiresTypeHint(entityType)) {
            writeType(type, sink);
        }
    }

    /**
     * Check if a given type requires a type hint (aka {@literal _class} attribute) when writing to the document.
     *
     * @param type must not be {@literal null}.
     * @return {@literal true} if not a simple type, {@link Collection} or type with custom write target.
     */
    private boolean requiresTypeHint(Class<?> type) {
        return !conversions.isSimpleType(type) && !ClassUtils.isAssignable(Collection.class, type)
                && !conversions.hasCustomWriteTarget(type, Document.class);
    }

    /**
     * Internal write conversion method which should be used for nested invocations.
     */
    @SuppressWarnings("unchecked")
    protected void writeInternal(Object obj, Bson bson, TypeDescriptor typeHint) {

        if (null == obj) {
            return;
        }

        Class<?> entityType = obj.getClass();
        Optional<Class<?>> optional = conversions.getCustomWriteTarget(entityType, Document.class);

        if (optional.isPresent()) {
            Document result = doConvert(obj, Document.class);
            BsonUtils.addAllToMap(bson, result);
            return;
        }

        if (Map.class.isAssignableFrom(entityType)) {
            writeMapInternal((Map<Object, Object>) obj, bson, MAP_TYPE_DESCRIPTOR);
            return;
        }

        if (Collection.class.isAssignableFrom(entityType)) {
            writeCollectionInternal((Collection<?>) obj, LIST_TYPE_DESCRIPTOR, (Collection<?>) bson);
            return;
        }

        MongoPersistentEntity entity = mappingContext.getRequiredPersistentEntity(entityType);
        writeInternal(obj, bson, entity);
        addCustomTypeKeyIfNecessary(typeHint, obj, bson);
    }

    protected void writeInternal(Object obj, Bson bson, MongoPersistentEntity entity) {

        if (obj == null) {
            return;
        }

        if (null == entity) {
            throw new MappingException("No mapping metadata found for entity of type " + obj.getClass().getName());
        }

        PersistentPropertyAccessor<?> accessor = new InstanceWrapper<>(obj);
        DocumentAccessor dbObjectAccessor = new DocumentAccessor(bson);
        MongoPersistentProperty idProperty = entity.getIdProperty();

        if (idProperty != null && !dbObjectAccessor.hasValue(idProperty)) {

            Object value = convertId(accessor.getProperty(idProperty), idProperty.getFieldType());

            if (value != null) {
                dbObjectAccessor.put(idProperty, value);
            }
        }

        writeProperties(bson, entity, accessor, dbObjectAccessor, idProperty);
    }

    private void writeProperties(Bson bson, MongoPersistentEntity entity, PersistentPropertyAccessor<?> accessor,
                                 DocumentAccessor dbObjectAccessor, MongoPersistentProperty idProperty) {

        // Write the properties
        for (MongoPersistentProperty prop : entity) {

            if (prop.equals(idProperty) || !prop.isWritable()) {
                continue;
            }

            Object value = accessor.getProperty(prop);

            if (value == null) {
                if (prop.writeNullValues()) {
                    dbObjectAccessor.put(prop, null);
                }
            } else if (!conversions.isSimpleType(value.getClass())) {
                writePropertyInternal(value, dbObjectAccessor, prop, accessor);
            } else {
                writeSimpleInternal(value, bson, prop, accessor);
            }
        }
    }

    @SuppressWarnings({ "unchecked" })
    protected void writePropertyInternal(Object obj, DocumentAccessor accessor, MongoPersistentProperty prop,
                                         PersistentPropertyAccessor<?> persistentPropertyAccessor) {

        if (obj == null) {
            return;
        }

        TypeDescriptor valueType = TypeDescriptor.forObject(obj);
        TypeDescriptor type = prop.getDescriptor();

        if (valueType.isCollectionLike()) {

            List<Object> collectionInternal = createCollection(CollectionUtils.asCollection(obj), prop);
            accessor.put(prop, collectionInternal);
            return;
        }

        if (valueType.isMap()) {

            Bson mapDbObj = createMap((Map<Object, Object>) obj, prop);
            accessor.put(prop, mapDbObj);
            return;
        }

        // Lookup potential custom target type
        Optional<Class<?>> basicTargetType = conversions.getCustomWriteTarget(obj.getClass());

        if (basicTargetType.isPresent()) {

            accessor.put(prop, doConvert(obj, basicTargetType.get()));
            return;
        }

        MongoPersistentEntity entity = valueType.isSubTypeOf(prop.getActualType())
                ? mappingContext.getRequiredPersistentEntity(obj.getClass())
                : mappingContext.getRequiredPersistentEntity(type.getType());

        Object existingValue = accessor.get(prop);
        Document document = existingValue instanceof Document ? (Document) existingValue : new Document();

        writeInternal(obj, document, entity);
        accessor.put(prop, document);

        addCustomTypeKeyIfNecessary(type, obj, document);
    }

    /**
     * Adds custom type information to the given {@link Document} if necessary. That is if the value is not the same as
     * the one given. This is usually the case if you store a subtype of the actual declared type of the property.
     *
     * @param type  can be {@literal null}.
     * @param value must not be {@literal null}.
     * @param bson  must not be {@literal null}.
     */
    protected void addCustomTypeKeyIfNecessary(TypeDescriptor type, Object value, Bson bson) {

        Class<?> reference = type != null ? type.getType() : Object.class;
        Class<?> valueType = ClassUtils.getUserClass(value.getClass());

        boolean notTheSameClass = !valueType.equals(reference);
        if (notTheSameClass) {
            writeType(TypeDescriptor.valueOf(valueType), bson);
        }
    }

    /**
     * Writes the given {@link Map} using the given {@link MongoPersistentProperty} information.
     *
     * @param map      must not {@literal null}.
     * @param property must not be {@literal null}.
     */
    protected Bson createMap(Map<Object, Object> map, MongoPersistentProperty property) {

        Assert.notNull(map, "Given map must not be null");
        Assert.notNull(property, "PersistentProperty must not be null");

        Document document = new Document();
        writeMapInternal(map, document, property.getDescriptor());

        if (!document.isEmpty() && !map.isEmpty()) {
            return document;
        }

        for (Map.Entry<Object, Object> entry : map.entrySet()) {

            Object key = entry.getKey();
            Object value = entry.getValue();

            if (conversions.isSimpleType(key.getClass())) {
                String simpleKey = prepareMapKey(key.toString());
                document.put(simpleKey, value);
            } else {
                throw new MappingException("Cannot use a complex object as a key value");
            }
        }

        return document;
    }


    /**
     * Writes the given {@link Collection} using the given {@link MongoPersistentProperty} information.
     *
     * @param collection must not be {@literal null}.
     * @param property must not be {@literal null}.
     */
    protected List<Object> createCollection(Collection<?> collection, MongoPersistentProperty property) {
        return writeCollectionInternal(collection, property.getDescriptor(), new ArrayList<>(collection.size()));
    }

    /**
     * Reads the given {@link Collection} into a collection of the given {@link TypeDescriptor}. Can be overridden by
     * subclasses.
     *
     * @param context must not be {@literal null}
     * @param source must not be {@literal null}
     * @param targetType the {@link Map} {@link TypeDescriptor} to be used to unmarshall this {@link Document}.
     * @since 3.2
     * @return the converted {@link Collection} or array, will never be {@literal null}.
     */
    @SuppressWarnings("unchecked")
    protected Object readCollectionOrArray(ConversionContext context, Collection<?> source,
                                           TypeDescriptor targetType) {

        Assert.notNull(targetType, "Target type must not be null");

        Class<?> collectionType = targetType.isSubTypeOf(Collection.class) //
                ? targetType.getType() //
                : List.class;


        TypeDescriptor componentType = targetType.getElementTypeDescriptor() != null //
                ? targetType.getElementTypeDescriptor() //
                : TypeDescriptorUtils.OBJECT;
        Class<?> rawComponentType = componentType.getType();

        Collection<Object> items = targetType.getType().isArray() //
                ? new ArrayList<>(source.size()) //
                : CollectionUtils.create(collectionType, rawComponentType, source.size());

        if (source.isEmpty()) {
            return getPotentiallyConvertedSimpleRead(items, targetType.getType());
        }

        for (Object element : source) {
            items.add(element != null ? context.convert(element, componentType) : element);
        }

        return getPotentiallyConvertedSimpleRead(items, targetType.getType());
    }

    /**
     * Reads the given {@link Document} into a {@link Map}. will recursively resolve nested {@link Map}s as well. Can be
     * overridden by subclasses.
     *
     * @param context must not be {@literal null}
     * @param bson must not be {@literal null}
     * @param targetType the {@link Map} {@link TypeDescriptor} to be used to unmarshall this {@link Document}.
     * @return the converted {@link Map}, will never be {@literal null}.
     */
    protected Map<Object, Object> readMap(ConversionContext context, Bson bson, TypeDescriptor targetType) {

        Assert.notNull(bson, "Document must not be null");
        Assert.notNull(targetType, "TypeInformation must not be null");

        Class<?> mapType = readType(bson, targetType).getType();

        TypeDescriptor keyType = targetType.getMapKeyTypeDescriptor();
        TypeDescriptor valueType = targetType.getMapValueTypeDescriptor() == null ? TypeDescriptorUtils.OBJECT
                : targetType.getMapValueTypeDescriptor();

        Class<?> rawKeyType = keyType != null ? keyType.getType() : Object.class;
        Class<?> rawValueType = valueType.getType();

        Map<String, Object> sourceMap = BsonUtils.asMap(bson);
        Map<Object, Object> map = CollectionUtils.createMap(mapType, rawKeyType, sourceMap.keySet().size());

        sourceMap.forEach((k, v) -> {

            Object key = potentiallyUnescapeMapKey(k);

            if (!rawKeyType.isAssignableFrom(key.getClass())) {
                key = doConvert(key, rawKeyType);
            }

            Object value = v;
            map.put(key, value == null ? value : context.convert(value, valueType));

        });

        return map;
    }


    /**
     * Populates the given {@link Collection sink} with converted values from the given {@link Collection source}.
     *
     * @param source the collection to create a {@link Collection} for, must not be {@literal null}.
     * @param type the {@link TypeDescriptor} to consider or {@literal null} if unknown.
     * @param sink the {@link Collection} to write to.
     */
    @SuppressWarnings("unchecked")
    private List<Object> writeCollectionInternal(Collection<?> source, TypeDescriptor type,
                                                 Collection<?> sink) {

        TypeDescriptor componentType = null;

        List<Object> collection = sink instanceof List ? (List<Object>) sink : new ArrayList<>(sink);

        if (type != null) {
            componentType = type.getElementTypeDescriptor();
        }

        for (Object element : source) {

            Class<?> elementType = element == null ? null : element.getClass();

            if (elementType == null || conversions.isSimpleType(elementType)) {
                collection.add(getPotentiallyConvertedSimpleWrite(element,
                        componentType != null ? componentType.getType() : Object.class));
            } else if (element instanceof Collection || elementType.isArray()) {

                Collection<?> objects = CollectionUtils.asCollection(element);
                collection.add(writeCollectionInternal(objects, componentType, new ArrayList<>(objects.size())));
            } else {
                Document document = new Document();
                writeInternal(element, document, componentType);
                collection.add(document);
            }
        }

        return collection;
    }

    /**
     * Writes the given {@link Map} to the given {@link Document} considering the given {@link TypeDescriptor}.
     *
     * @param obj must not be {@literal null}.
     * @param bson must not be {@literal null}.
     * @param propertyType must not be {@literal null}.
     */
    protected Bson writeMapInternal(Map<Object, Object> obj, Bson bson, TypeDescriptor propertyType) {

        for (Map.Entry<Object, Object> entry : obj.entrySet()) {

            Object key = entry.getKey();
            Object val = entry.getValue();

            if (conversions.isSimpleType(key.getClass())) {

                String simpleKey = prepareMapKey(key);
                if (val == null || conversions.isSimpleType(val.getClass())) {
                    writeSimpleInternal(val, bson, simpleKey);
                } else if (val instanceof Collection || val.getClass().isArray()) {
                    BsonUtils.addToMap(bson, simpleKey,
                            writeCollectionInternal(CollectionUtils.asCollection(val), propertyType.getMapValueTypeDescriptor(), new ArrayList<>()));
                } else {
                    Document document = new Document();
                    TypeDescriptor valueTypeInfo = propertyType.isMap() ? propertyType.getMapValueTypeDescriptor()
                            : TypeDescriptorUtils.OBJECT;
                    writeInternal(val, document, valueTypeInfo);
                    BsonUtils.addToMap(bson, simpleKey, document);
                }
            } else {
                throw new MappingException("Cannot use a complex object as a key value");
            }
        }

        return bson;
    }

    /**
     * Prepares the given {@link Map} key to be converted into a {@link String}. Will invoke potentially registered custom
     * conversions and escape dots from the result as they're not supported as {@link Map} key in MongoDB.
     *
     * @param key must not be {@literal null}.
     */
    private String prepareMapKey(Object key) {

        Assert.notNull(key, "Map key must not be null");

        String convertedKey = potentiallyConvertMapKey(key);
        return potentiallyEscapeMapKey(convertedKey);
    }

    /**
     * Potentially replaces dots in the given map key with the configured map key replacement if configured or aborts
     * conversion if none is configured.
     *
     * @see #setMapKeyDotReplacement(String)
     * @param source must not be {@literal null}.
     */
    protected String potentiallyEscapeMapKey(String source) {

        if (!source.contains(".")) {
            return source;
        }

        if (mapKeyDotReplacement == null) {
            throw new MappingException(String.format(
                    "Map key %s contains dots but no replacement was configured; Make"
                            + " sure map keys don't contain dots in the first place or configure an appropriate replacement",
                    source));
        }

        return StringUtils.replace(source, ".", mapKeyDotReplacement);
    }

    /**
     * Returns a {@link String} representation of the given {@link Map} key
     *
     * @param key
     */
    private String potentiallyConvertMapKey(Object key) {

        if (key instanceof String) {
            return (String) key;
        }

        return conversions.hasCustomWriteTarget(key.getClass(), String.class)
                ? (String) getPotentiallyConvertedSimpleWrite(key, Object.class)
                : key.toString();
    }

    /**
     * Translates the map key replacements in the given key just read with a dot in case a map key replacement has been
     * configured.
     *
     * @param source must not be {@literal null}.
     */
    protected String potentiallyUnescapeMapKey(String source) {
        return mapKeyDotReplacement == null ? source : StringUtils.replace(source, mapKeyDotReplacement, ".");
    }

    /**
     * Writes the given simple value to the given {@link Document}. Will store enum names for enum values.
     *
     * @param value can be {@literal null}.
     * @param bson must not be {@literal null}.
     * @param key must not be {@literal null}.
     */
    private void writeSimpleInternal(Object value, Bson bson, String key) {
        BsonUtils.addToMap(bson, key, getPotentiallyConvertedSimpleWrite(value, Object.class));
    }

    private void writeSimpleInternal(Object value, Bson bson, MongoPersistentProperty property,
                                     PersistentPropertyAccessor<?> persistentPropertyAccessor) {
        DocumentAccessor accessor = new DocumentAccessor(bson);

        accessor.put(property, getPotentiallyConvertedSimpleWrite(value,
                property.hasExplicitWriteTarget() ? property.getFieldType() : Object.class));
    }

    /**
     * Checks whether we have a custom conversion registered for the given value into an arbitrary simple Mongo type.
     * Returns the converted value if so. If not, we perform special enum handling or simply return the value as is.
     */
    private Object getPotentiallyConvertedSimpleWrite(Object value, Class<?> typeHint) {

        if (value == null) {
            return null;
        }

        if (typeHint != null && Object.class != typeHint) {

            if (conversionService.canConvert(value.getClass(), typeHint)) {
                value = doConvert(value, typeHint);
            }
        }

        Optional<Class<?>> customTarget = conversions.getCustomWriteTarget(value.getClass());

        if (customTarget.isPresent()) {
            return doConvert(value, customTarget.get());
        }

        if (ObjectUtils.isArray(value)) {

            if (value instanceof byte[]) {
                return value;
            }
            return CollectionUtils.asCollection(value);
        }
        // 枚举存值
        return Enum.class.isAssignableFrom(value.getClass()) ? ((Enum<?>) value).name() : value;
    }

    /**
     * Checks whether we have a custom conversion for the given simple object. Converts the given value if so, applies
     * {@link Enum} handling or returns the value as is. Can be overridden by subclasses.
     *
     * @since 3.2
     */
    protected Object getPotentiallyConvertedSimpleRead(Object value, TypeDescriptor target) {
        return getPotentiallyConvertedSimpleRead(value, target.getType());
    }

    /**
     * Checks whether we have a custom conversion for the given simple object. Converts the given value if so, applies
     * {@link Enum} handling or returns the value as is.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object getPotentiallyConvertedSimpleRead(Object value, Class<?> target) {

        if (target == null) {
            return value;
        }

        if (conversions.hasCustomReadTarget(value.getClass(), target)) {
            return doConvert(value, target);
        }

        if (ClassUtils.isAssignableValue(target, value)) {
            return value;
        }

        if (Enum.class.isAssignableFrom(target)) {
            return Enum.valueOf((Class<Enum>) target, value.toString());
        }

        return doConvert(value, target);
    }

    @Override
    public Object convertToMongoType(Object obj, TypeDescriptor typeDescriptor) {
        if (obj == null) {
            return null;
        }
        Optional<Class<?>> optional = conversions.getCustomWriteTarget(obj.getClass());
        if (optional.isPresent()) {
            return doConvert(obj, optional.get());
        }
        if (conversions.isSimpleType(obj.getClass())) {
            Class<?> conversionTargetType;

            if (typeDescriptor != null && conversions.isSimpleType(typeDescriptor.getType())) {
                conversionTargetType = typeDescriptor.getType();
            } else {
                conversionTargetType = Object.class;
            }

            return getPotentiallyConvertedSimpleWrite(obj, conversionTargetType);
        }

        if (obj instanceof List<?>) {
            return maybeConvertList((List<?>) obj, typeDescriptor);
        }

        if (obj instanceof Document) {
            Document document = (Document) obj;
            Document newValueDocument = new Document();
            for (String vk : document.keySet()) {
                Object o = document.get(vk);
                newValueDocument.put(vk, convertToMongoType(o, typeDescriptor));
            }
            return newValueDocument;
        }

        if (obj instanceof DBObject) {
            DBObject dbObject = (DBObject) obj;
            Document newValueDbo = new Document();
            for (String vk : dbObject.keySet()) {

                Object o = dbObject.get(vk);
                newValueDbo.put(vk, convertToMongoType(o, typeDescriptor));
            }

            return newValueDbo;
        }

        if (obj instanceof Map) {

            Document result = new Document();

            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) obj).entrySet()) {
                result.put(entry.getKey().toString(), convertToMongoType(entry.getValue(), typeDescriptor));
            }

            return result;
        }

        if (obj.getClass().isArray()) {
            return maybeConvertList(Arrays.asList((Object[]) obj), typeDescriptor);
        }
        if (obj instanceof Collection<?>) {
            return maybeConvertList((Collection<?>) obj, typeDescriptor);
        }

        Document newDocument = new Document();
        this.write(obj, newDocument);

        if (typeDescriptor == null) {
            return removeTypeInfo(newDocument, true);
        }

        return !obj.getClass().equals(typeDescriptor.getType()) ? newDocument : removeTypeInfo(newDocument, true);
    }

    @Override
    public ConversionService getConversionService() {
        return conversionService;
    }

    @Override
    public CodecRegistry getCodecRegistry() {
        return codecRegistryProvider != null ? codecRegistryProvider.getCodecRegistry()
                : MongoConverter.super.getCodecRegistry();
    }

    public List<Object> maybeConvertList(Iterable<?> source, TypeDescriptor descriptor) {

        List<Object> newDbl = new ArrayList<>();

        for (Object element : source) {
            newDbl.add(convertToMongoType(element, descriptor));
        }

        return newDbl;
    }


    /**
     * Removes the type information from the entire conversion result.
     *
     * @param object
     * @param recursively whether to apply the removal recursively
     * @return
     */
    @SuppressWarnings("unchecked")
    private Object removeTypeInfo(Object object, boolean recursively) {

        if (!(object instanceof Document)) {
            return object;
        }

        Document document = (Document) object;
        String keyToRemove = null;

        for (String key : document.keySet()) {

            if (recursively) {

                Object value = document.get(key);

                if (value instanceof BasicDBList) {
                    for (Object element : (BasicDBList) value) {
                        removeTypeInfo(element, recursively);
                    }
                } else if (value instanceof List) {
                    for (Object element : (List<Object>) value) {
                        removeTypeInfo(element, recursively);
                    }
                } else {
                    removeTypeInfo(value, recursively);
                }
            }

//            if (getTypeMapper().isTypeKey(key)) {
//
//                keyToRemove = key;
//
//                if (!recursively) {
//                    break;
//                }
//            }
        }

        if (keyToRemove != null) {
            document.remove(keyToRemove);
        }

        return document;
    }

    /**
     * Conversion context defining an interface for graph-traversal-based conversion of documents. Entrypoint for
     * recursive conversion of {@link Document} and other types.
     *
     * @since 3.4.3
     */
    protected interface ConversionContext {

        /**
         * Converts a source object into {@link TypeDescriptor target}.
         *
         * @param source must not be {@literal null}.
         * @param typeHint must not be {@literal null}.
         * @return the converted object.
         */
        default <S extends Object> S convert(Object source, TypeDescriptor typeHint) {
            return convert(source, typeHint, this);
        }

        /**
         * Converts a source object into {@link TypeDescriptor target}.
         *
         * @param source must not be {@literal null}.
         * @param typeHint must not be {@literal null}.
         * @param context must not be {@literal null}.
         * @return the converted object.
         */
        <S extends Object> S convert(Object source, TypeDescriptor typeHint, ConversionContext context);

        CustomConversions getCustomConversions();

        MongoConverter getSourceConverter();

    }

    /**
     * Conversion context holding references to simple {@link ValueConverter} and {@link ContainerValueConverter}.
     * Entrypoint for recursive conversion of {@link Document} and other types.
     *
     * @since 3.2
     */
    protected static class DefaultConversionContext implements ConversionContext {

        final MongoConverter sourceConverter;
        final CustomConversions conversions;
        final ContainerValueConverter<Bson> documentConverter;
        final ContainerValueConverter<Collection<?>> collectionConverter;
        final ContainerValueConverter<Bson> mapConverter;
        final ValueConverter<Object> elementConverter;

        DefaultConversionContext(MongoConverter sourceConverter, CustomConversions conversions
                , ContainerValueConverter<Bson> documentConverter
                , ContainerValueConverter<Collection<?>> collectionConverter
                , ContainerValueConverter<Bson> mapConverter
                , ValueConverter<Object> elementConverter) {
            this.sourceConverter = sourceConverter;
            this.conversions = conversions;
            this.documentConverter = documentConverter;
            this.collectionConverter = collectionConverter;
            this.mapConverter = mapConverter;
            this.elementConverter = elementConverter;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <S> S convert(Object source, TypeDescriptor typeHint, ConversionContext context) {
            Assert.notNull(source, "Source must not be null");
            Assert.notNull(typeHint, "TypeInformation must not be null");

            if (conversions.hasCustomReadTarget(source.getClass(), typeHint.getType())) {
                return (S) elementConverter.convert(source, typeHint);
            }

            if (source instanceof Collection<?>) {

                Collection<?> collection = (Collection<?>) source;
                Class<?> rawType = typeHint.getType();
                if (!Object.class.equals(rawType) && !String.class.equals(rawType)) {

                    if (!rawType.isArray() && !ClassUtils.isAssignable(Iterable.class, rawType)) {

                        throw new MappingException(
                                String.format(INCOMPATIBLE_TYPES, source, source.getClass(), rawType));
                    }
                }

                if (typeHint.isCollectionLike()) {
                    return (S) collectionConverter.convert(context, collection, typeHint);
                }
            }

            if (typeHint.isMap()) {

                if (ClassUtils.isAssignable(Document.class, typeHint.getType())) {
                    return (S) documentConverter.convert(context, BsonUtils.asBson(source), typeHint);
                }

                if (BsonUtils.supportsBson(source)) {
                    return (S) mapConverter.convert(context, BsonUtils.asBson(source), typeHint);
                }

                throw new IllegalArgumentException(
                        String.format("Expected map like structure but found %s", source.getClass()));
            }

            if (BsonUtils.supportsBson(source)) {
                return (S) documentConverter.convert(context, BsonUtils.asBson(source), typeHint);
            }

            return (S) elementConverter.convert(source, typeHint);
        }

        @Override
        public CustomConversions getCustomConversions() {
            return conversions;
        }

        @Override
        public MongoConverter getSourceConverter() {
            return sourceConverter;
        }

        /**
         * Converts a simple {@code source} value into {@link TypeDescriptor the target type}.
         *
         * @param <T>
         */
        interface ValueConverter<T> {

            Object convert(T source, TypeDescriptor typeHint);

        }

        /**
         * Converts a container {@code source} value into {@link TypeDescriptor the target type}. Containers may
         * recursively apply conversions for entities, collections, maps, etc.
         *
         * @param <T>
         */
        interface ContainerValueConverter<T> {

            Object convert(ConversionContext context, T source, TypeDescriptor typeHint);

        }
    }

    static class MongoDbPropertyValueProvider implements PropertyValueProvider {
        final ConversionContext context;
        final DocumentAccessor accessor;

        public MongoDbPropertyValueProvider(ConversionContext context, DocumentAccessor accessor) {
            this.context = context;
            this.accessor = accessor;
        }

        @Override
        public Object getPropertyValue(MongoPersistentProperty property) {
            Object value = accessor.get(property);
            if (value == null) {
                return null;
            }
            return context.convert(value, property.getDescriptor());
        }
    }
}
