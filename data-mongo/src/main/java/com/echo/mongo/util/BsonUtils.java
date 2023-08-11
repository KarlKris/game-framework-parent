package com.echo.mongo.util;

import com.echo.common.util.ObjectUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClientSettings;
import org.bson.BSONObject;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.Collections;
import java.util.Map;

/**
 * @author: li-yuanwen
 */
public class BsonUtils {

    public static final Document EMPTY_DOCUMENT = new EmptyDocument();

    /**
     * Remove {@code _id : null} from the given {@link Bson} if present.
     *
     * @param bson must not be {@literal null}.
     * @since 3.2
     */
    public static boolean removeNullId(Bson bson) {

        if (!contains(bson, "_id", null)) {
            return false;
        }

        removeFrom(bson, "_id");
        return true;
    }

    /**
     * Check if a given entry (key/value pair) is present in the given {@link Bson}.
     *
     * @param bson must not be {@literal null}.
     * @param key must not be {@literal null}.
     * @param value can be {@literal null}.
     * @return {@literal true} if (key/value pair) is present.
     * @since 3.2
     */
    public static boolean contains(Bson bson, String key,Object value) {

        if (bson instanceof Document) {
            Document document = (Document) bson;
            return document.containsKey(key) && ObjectUtils.nullSafeEquals(document.get(key), value);
        }
        if (bson instanceof BSONObject) {
            BSONObject bsonObject = (BSONObject) bson;
            return bsonObject.containsField(key) && ObjectUtils.nullSafeEquals(bsonObject.get(key), value);
        }

        Map<String, Object> map = asMap(bson);
        return map.containsKey(key) && ObjectUtils.nullSafeEquals(map.get(key), value);
    }

    /**
     * Remove the given {@literal key} from the {@link Bson} value.
     *
     * @param bson must not be {@literal null}.
     * @param key must not be {@literal null}.
     * @since 3.2
     */
    static void removeFrom(Bson bson, String key) {

        if (bson instanceof Document) {
            Document document = (Document) bson;
            document.remove(key);
            return;
        }

        if (bson instanceof BSONObject) {
            BSONObject bsonObject = (BSONObject) bson;
            bsonObject.removeField(key);
            return;
        }

        throw new IllegalArgumentException(
                String.format("Cannot remove from %s given Bson must be a Document or BSONObject.", bson.getClass()));
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Bson bson, String key) {
        return (T) asMap(bson).get(key);
    }

    /**
     * Return the {@link Bson} object as {@link Map}. Depending on the input type, the return value can be either a casted
     * version of {@code bson} or a converted (detached from the original value).
     *
     * @param bson
     * @return
     */
    public static Map<String, Object> asMap(Bson bson) {
        return asMap(bson, MongoClientSettings.getDefaultCodecRegistry());
    }

    /**
     * Return the {@link Bson} object as {@link Map}. Depending on the input type, the return value can be either a casted
     * version of {@code bson} or a converted (detached from the original value) using the given {@link CodecRegistry} to
     * obtain {@link org.bson.codecs.Codec codecs} that might be required for conversion.
     *
     * @param bson can be {@literal null}.
     * @param codecRegistry must not be {@literal null}.
     * @return never {@literal null}. Returns an empty {@link Map} if input {@link Bson} is {@literal null}.
     * @since 4.0
     */
    public static Map<String, Object> asMap(Bson bson, CodecRegistry codecRegistry) {

        if (bson == null) {
            return Collections.emptyMap();
        }

        if (bson instanceof Document) {
            return (Document) bson;
        }
        if (bson instanceof BasicDBObject) {
            return (BasicDBObject) bson;
        }
        if (bson instanceof DBObject) {
            return ((DBObject) bson).toMap();
        }

        return new Document((Map) bson.toBsonDocument(Document.class, codecRegistry));
    }

    public static void addToMap(Bson bson, String key, Object value) {

        if (bson instanceof Document) {
            Document document = (Document) bson;
            document.put(key, value);
            return;
        }
        if (bson instanceof BSONObject) {
            BSONObject bsonObject = (BSONObject) bson;
            bsonObject.put(key, value);
            return;
        }

        throw new IllegalArgumentException(String.format(
                "Cannot add key/value pair to %s; as map given Bson must be a Document or BSONObject", bson.getClass()));
    }

    /**
     * Returns the given source can be used/converted as {@link Bson}.
     *
     * @param source
     * @return {@literal true} if the given source can be converted to {@link Bson}.
     * @since 3.2.3
     */
    public static boolean supportsBson(Object source) {
        return source instanceof DBObject || source instanceof Map;
    }


    /**
     * Returns the given source object as {@link Bson}, i.e. {@link Document}s and maps as is or throw
     * {@link IllegalArgumentException}.
     *
     * @param source
     * @return the converted/casted source object.
     * @throws IllegalArgumentException if {@code source} cannot be converted/cast to {@link Bson}.
     * @since 3.2.3
     * @see #supportsBson(Object)
     */
    @SuppressWarnings("unchecked")
    public static Bson asBson(Object source) {

        if (source instanceof Document) {
            return (Document) source;
        }

        if (source instanceof BasicDBObject) {
            return (BasicDBObject) source;
        }

        if (source instanceof DBObject) {
            return new Document(((DBObject) source).toMap());
        }

        if (source instanceof Map) {
            return new Document((Map<String, Object>) source);
        }

        throw new IllegalArgumentException(String.format("Cannot convert %s to Bson", source));
    }

    /**
     * Resolve the value for a given key. If the given {@link Bson} value contains the key the value is immediately
     * returned. If not and the key contains a path using the dot ({@code .}) notation it will try to resolve the path by
     * inspecting the individual parts. If one of the intermediate ones is {@literal null} or cannot be inspected further
     * (wrong) type, {@literal null} is returned.
     *
     * @param bson the source to inspect. Must not be {@literal null}.
     * @param key the key to lookup. Must not be {@literal null}.
     * @return can be {@literal null}.
     * @since 3.0.8
     */
    public static Object resolveValue(Bson bson, String key) {
        return resolveValue(asMap(bson), key);
    }

    /**
     * Resolve the value for a given key. If the given {@link Map} value contains the key the value is immediately
     * returned. If not and the key contains a path using the dot ({@code .}) notation it will try to resolve the path by
     * inspecting the individual parts. If one of the intermediate ones is {@literal null} or cannot be inspected further
     * (wrong) type, {@literal null} is returned.
     *
     * @param source the source to inspect. Must not be {@literal null}.
     * @param key the key to lookup. Must not be {@literal null}.
     * @return can be {@literal null}.
     * @since 4.1
     */
    public static Object resolveValue(Map<String, Object> source, String key) {

        if (source.containsKey(key) || !key.contains(".")) {
            return source.get(key);
        }

        String[] parts = key.split("\\.");

        for (int i = 1; i < parts.length; i++) {

            Object result = source.get(parts[i - 1]);

            if (!(result instanceof Bson)) {
                return null;
            }

            source = asMap((Bson) result);
        }

        return source.get(parts[parts.length - 1]);
    }

    /**
     * Returns whether the underlying {@link Bson bson} has a value ({@literal null} or non-{@literal null}) for the given
     * {@code key}.
     *
     * @param bson the source to inspect. Must not be {@literal null}.
     * @param key the key to lookup. Must not be {@literal null}.
     * @return {@literal true} if no non {@literal null} value present.
     * @since 3.0.8
     */
    public static boolean hasValue(Bson bson, String key) {

        Map<String, Object> source = asMap(bson);

        if (source.get(key) != null) {
            return true;
        }

        if (!key.contains(".")) {
            return false;
        }

        String[] parts = key.split("\\.");

        Object result;

        for (int i = 1; i < parts.length; i++) {

            result = source.get(parts[i - 1]);
            source = getAsMap(result);

            if (source == null) {
                return false;
            }
        }

        return source.containsKey(parts[parts.length - 1]);
    }


    /**
     * Returns the given source object as map, i.e. {@link Document}s and maps as is or {@literal null} otherwise.
     *
     * @param source can be {@literal null}.
     * @return can be {@literal null}.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getAsMap(Object source) {

        if (source instanceof Document) {
            Document document = (Document) source;
            return document;
        }

        if (source instanceof BasicDBObject) {
            BasicDBObject basicDBObject = (BasicDBObject) source;
            return basicDBObject;
        }

        if (source instanceof DBObject) {
            DBObject dbObject = (DBObject) source;
            return dbObject.toMap();
        }

        if (source instanceof Map) {
            return (Map<String, Object>) source;
        }

        return null;
    }


    /**
     * Add all entries from the given {@literal source} {@link Map} to the {@literal target}.
     *
     * @param target must not be {@literal null}.
     * @param source must not be {@literal null}.
     * @since 3.2
     */
    public static void addAllToMap(Bson target, Map<String, ?> source) {

        if (target instanceof Document) {
            Document document = (Document) target;
            document.putAll(source);
            return;
        }

        if (target instanceof BSONObject) {
            BSONObject bsonObject = (BSONObject) target;
            bsonObject.putAll(source);
            return;
        }

        throw new IllegalArgumentException(
                String.format("Cannot add all to %s; Given Bson must be a Document or BSONObject.", target.getClass()));
    }
}
