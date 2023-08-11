package com.echo.mongo.schema;

import com.echo.common.util.ObjectUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Interface that can be implemented by objects that know how to serialize themselves to JSON schema using
 * {@link #toDocument()}.
 * <br />
 * This class also declares factory methods for type-specific {@link JsonSchemaObject schema objects} such as
 * {@link #string()} or {@link #object()}. For example:
 *
 * <pre class="code">
 * JsonSchemaProperty.object("address").properties(JsonSchemaProperty.string("city").minLength(3));
 * </pre>
 */
public interface JsonSchemaObject {


    /**
     * Type represents either a JSON schema {@literal type} or a MongoDB specific {@literal bsonType}.
     */
    interface Type {

        // BSON TYPES
        Type OBJECT_ID = bsonTypeOf("objectId");
        Type REGULAR_EXPRESSION = bsonTypeOf("regex");
        Type DOUBLE = bsonTypeOf("double");
        Type BINARY_DATA = bsonTypeOf("binData");
        Type DATE = bsonTypeOf("date");
        Type JAVA_SCRIPT = bsonTypeOf("javascript");
        Type INT_32 = bsonTypeOf("int");
        Type INT_64 = bsonTypeOf("long");
        Type DECIMAL_128 = bsonTypeOf("decimal");
        Type TIMESTAMP = bsonTypeOf("timestamp");

        Set<Type> BSON_TYPES = new HashSet<>(Arrays.asList(OBJECT_ID, REGULAR_EXPRESSION, DOUBLE, BINARY_DATA, DATE,
                JAVA_SCRIPT, INT_32, INT_64, DECIMAL_128, TIMESTAMP));

        // JSON SCHEMA TYPES
        Type OBJECT = jsonTypeOf("object");
        Type ARRAY = jsonTypeOf("array");
        Type NUMBER = jsonTypeOf("number");
        Type BOOLEAN = jsonTypeOf("boolean");
        Type STRING = jsonTypeOf("string");
        Type NULL = jsonTypeOf("null");

        Set<Type> JSON_TYPES = new HashSet<>(Arrays.asList(OBJECT, ARRAY, NUMBER, BOOLEAN, STRING, NULL));

        /**
         * @return a constant {@link Type} representing {@code bsonType : 'objectId' }.
         */
        static Type objectIdType() {
            return OBJECT_ID;
        }

        /**
         * @return a constant {@link Type} representing {@code bsonType : 'regex' }.
         */
        static Type regexType() {
            return REGULAR_EXPRESSION;
        }

        /**
         * @return a constant {@link Type} representing {@code bsonType : 'double' }.
         */
        static Type doubleType() {
            return DOUBLE;
        }

        /**
         * @return a constant {@link Type} representing {@code bsonType : 'binData' }.
         */
        static Type binaryType() {
            return BINARY_DATA;
        }

        /**
         * @return a constant {@link Type} representing {@code bsonType : 'date' }.
         */
        static Type dateType() {
            return DATE;
        }

        /**
         * @return a constant {@link Type} representing {@code bsonType : 'javascript' }.
         */
        static Type javascriptType() {
            return JAVA_SCRIPT;
        }

        /**
         * @return a constant {@link Type} representing {@code bsonType : 'int' }.
         */
        static Type intType() {
            return INT_32;
        }

        /**
         * @return a constant {@link Type} representing {@code bsonType : 'long' }.
         */
        static Type longType() {
            return INT_64;
        }

        /**
         * @return a constant {@link Type} representing {@code bsonType : 'decimal128' }.
         */
        static Type bigDecimalType() {
            return DECIMAL_128;
        }

        /**
         * @return a constant {@link Type} representing {@code bsonType : 'timestamp' }.
         */
        static Type timestampType() {
            return TIMESTAMP;
        }

        /**
         * @return a constant {@link Type} representing {@code type : 'object' }.
         */
        static Type objectType() {
            return OBJECT;
        }

        /**
         * @return a constant {@link Type} representing {@code type : 'array' }.
         */
        static Type arrayType() {
            return ARRAY;
        }

        /**
         * @return a constant {@link Type} representing {@code type : 'number' }.
         */
        static Type numberType() {
            return NUMBER;
        }

        /**
         * @return a constant {@link Type} representing {@code type : 'boolean' }.
         */
        static Type booleanType() {
            return BOOLEAN;
        }

        /**
         * @return a constant {@link Type} representing {@code type : 'string' }.
         */
        static Type stringType() {
            return STRING;
        }

        /**
         * @return a constant {@link Type} representing {@code type : 'null' }.
         */
        static Type nullType() {
            return NULL;
        }

        /**
         * @return new {@link Type} representing the given {@code bsonType}.
         */
        static Type bsonTypeOf(String name) {
            return new BsonType(name);
        }

        /**
         * @return new {@link Type} representing the given {@code type}.
         */
        static Type jsonTypeOf(String name) {
            return new JsonType(name);
        }

        /**
         * Create a {@link Type} with its default {@link Type#representation() representation} via the name.
         *
         * @param name must not be {@literal null}.
         * @return the matching type instance.
         * @since 2.2
         */
        static Type of(String name) {

            Type type = jsonTypeOf(name);
            if (jsonTypes().contains(type)) {
                return type;
            }

            return bsonTypeOf(name);
        }

        /**
         * @return all known JSON types.
         */
        static Set<Type> jsonTypes() {
            return JSON_TYPES;
        }

        /**
         * @return all known BSON types.
         */
        static Set<Type> bsonTypes() {
            return BSON_TYPES;
        }

        /**
         * Get the {@link Type} representation. Either {@code type} or {@code bsonType}.
         *
         * @return never {@literal null}.
         */
        String representation();

        /**
         * Get the {@link Type} value. Like {@literal string}, {@literal number},...
         *
         * @return never {@literal null}.
         */
        Object value();

        /**
         * Get the {@literal bsonType} representation of the given type.
         *
         * @return never {@literal null}.
         * @since 2.2
         */
        default Type toBsonType() {

            if (representation().equals("bsonType")) {
                return this;
            }

            if (value().equals(Type.booleanType().value())) {
                return bsonTypeOf("bool");
            }
            if (value().equals(Type.numberType().value())) {
                return bsonTypeOf("long");
            }

            return bsonTypeOf((String) value());
        }

        /**
         * @author Christpoh Strobl
         * @since 2.1
         */
        class JsonType implements Type {

            private final String name;

            public JsonType(String name) {
                this.name = name;
            }

            @Override
            public String representation() {
                return "type";
            }

            @Override
            public String value() {
                return name;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (o == null || getClass() != o.getClass())
                    return false;

                JsonType jsonType = (JsonType) o;

                return ObjectUtils.nullSafeEquals(name, jsonType.name);
            }

            @Override
            public int hashCode() {
                return ObjectUtils.nullSafeHashCode(name);
            }
        }

        /**
         * @author Christpoh Strobl
         * @since 2.1
         */
        class BsonType implements Type {

            private final String name;

            BsonType(String name) {
                this.name = name;
            }

            @Override
            public String representation() {
                return "bsonType";
            }

            @Override
            public String value() {
                return name;
            }

            @Override
            public boolean equals(Object o) {

                if (this == o)
                    return true;
                if (o == null || getClass() != o.getClass())
                    return false;

                BsonType bsonType = (BsonType) o;

                return ObjectUtils.nullSafeEquals(name, bsonType.name);
            }

            @Override
            public int hashCode() {
                return ObjectUtils.nullSafeHashCode(name);
            }
        }
    }
}
