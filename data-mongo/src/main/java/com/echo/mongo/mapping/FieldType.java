package com.echo.mongo.mapping;

import org.bson.types.*;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * Enumeration of field value types that can be used to represent a {@link org.bson.Document} field value. This
 * enumeration contains a subset of {@link org.bson.BsonType} that is supported by the mapping and conversion
 * components.
 * <br />
 * Bson types are identified by a {@code byte} {@link #getBsonType() value}. This enumeration typically returns the
 * according bson type value except for {@link #IMPLICIT} which is a marker to derive the field type from a property.
 *
 * @see org.bson.BsonType
 */
public enum FieldType {


    /**
     * Implicit type that is derived from the property value.
     */
    IMPLICIT(-1, Object.class), //
    DOUBLE(1, Double.class), //
    STRING(2, String.class), //
    ARRAY(4, Object[].class), //
    BINARY(5, Binary.class), //
    OBJECT_ID(7, ObjectId.class), //
    BOOLEAN(8, Boolean.class), //
    DATE_TIME(9, Date.class), //
    PATTERN(11, Pattern.class), //
    SCRIPT(13, Code.class), //
    INT32(15, Integer.class), //
    TIMESTAMP(16, BSONTimestamp.class), //
    INT64(17, Long.class), //
    DECIMAL128(18, Decimal128.class);

    private final int bsonType;
    private final Class<?> javaClass;

    FieldType(int bsonType, Class<?> javaClass) {

        this.bsonType = bsonType;
        this.javaClass = javaClass;
    }

    /**
     * Returns the BSON type identifier. Can be {@code -1} if {@link FieldType} maps to a synthetic Bson type.
     *
     * @return the BSON type identifier. Can be {@code -1} if {@link FieldType} maps to a synthetic Bson type.
     */
    public int getBsonType() {
        return bsonType;
    }

    /**
     * Returns the Java class used to represent the type.
     *
     * @return the Java class used to represent the type.
     */
    public Class<?> getJavaClass() {
        return javaClass;
    }
}
