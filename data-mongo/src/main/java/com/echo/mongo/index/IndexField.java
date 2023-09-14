package com.echo.mongo.index;

import com.echo.common.util.ObjectUtils;
import com.echo.common.util.StringUtils;

/**
 * @author: li-yuanwen
 */
public final class IndexField {

    enum Type {
        GEO, TEXT, DEFAULT,

        /**
         * @since 2.2
         */
        HASH,

        /**
         * @since 3.3
         */
        WILDCARD;
    }

    private final String key;
    private final Direction direction;
    private final Type type;
    private final Float weight;

    private IndexField(String key, Direction direction, Type type) {
        this(key, direction, type, Float.NaN);
    }

    private IndexField(String key, Direction direction, Type type, Float weight) {

        if (StringUtils.hasLength(key)) {
            throw new IllegalArgumentException("Key must not be null or empty");
        }

        if (Type.GEO.equals(type) || Type.TEXT.equals(type)) {
            if (direction != null) {
                throw new IllegalArgumentException("Geo/Text indexes must not have a direction");
            }
        } else {
            if (!(Type.HASH.equals(type) || Type.WILDCARD.equals(type))) {
                if (direction == null) {
                    throw new IllegalArgumentException("Default indexes require a direction");
                }
            }
        }

        this.key = key;
        this.direction = direction;
        this.type = type == null ? Type.DEFAULT : type;
        this.weight = weight == null ? Float.NaN : weight;
    }

    public static IndexField create(String key, Direction order) {

        if (order == null) {
            throw new IllegalArgumentException("Direction must not be null");
        }

        return new IndexField(key, order, Type.DEFAULT);
    }

    /**
     * Creates a {@literal hashed} {@link IndexField} for the given key.
     *
     * @param key must not be {@literal null} or empty.
     * @return new instance of {@link IndexField}.
     * @since 2.2
     */
    static IndexField hashed(String key) {
        return new IndexField(key, null, Type.HASH);
    }

    /**
     * Creates a {@literal wildcard} {@link IndexField} for the given key. The {@code key} must follow the
     * {@code fieldName.$**} notation.
     *
     * @param key must not be {@literal null} or empty.
     * @return new instance of {@link IndexField}.
     * @since 3.3
     */
    static IndexField wildcard(String key) {
        return new IndexField(key, null, Type.WILDCARD);
    }

    /**
     * Creates a geo {@link IndexField} for the given key.
     *
     * @param key must not be {@literal null} or empty.
     * @return new instance of {@link IndexField}.
     */
    public static IndexField geo(String key) {
        return new IndexField(key, null, Type.GEO);
    }

    /**
     * Creates a text {@link IndexField} for the given key.
     *
     * @since 1.6
     */
    public static IndexField text(String key, Float weight) {
        return new IndexField(key, null, Type.TEXT, weight);
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the direction of the {@link IndexField} or {@literal null} in case we have a geo index field.
     *
     * @return the direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Returns whether the {@link IndexField} is a geo index field.
     *
     * @return true if type is {@link Type#GEO}.
     */
    public boolean isGeo() {
        return Type.GEO.equals(type);
    }

    /**
     * Returns whether the {@link IndexField} is a text index field.
     *
     * @return true if type is {@link Type#TEXT}
     * @since 1.6
     */
    public boolean isText() {
        return Type.TEXT.equals(type);
    }

    /**
     * Returns whether the {@link IndexField} is a {@literal hashed}.
     *
     * @return {@literal true} if {@link IndexField} is hashed.
     * @since 2.2
     */
    public boolean isHashed() {
        return Type.HASH.equals(type);
    }

    /**
     * Returns whether the {@link IndexField} is contains a {@literal wildcard} expression.
     *
     * @return {@literal true} if {@link IndexField} contains a wildcard {@literal $**}.
     * @since 3.3
     */
    public boolean isWildcard() {
        return Type.WILDCARD.equals(type);
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof IndexField)) {
            return false;
        }

        IndexField other = (IndexField) obj;
        return this.key.equals(other.key) && ObjectUtils.nullSafeEquals(this.direction, other.direction)
                && this.type == other.type;
    }

    @Override
    public int hashCode() {

        int result = 17;
        result += 31 * ObjectUtils.nullSafeHashCode(key);
        result += 31 * ObjectUtils.nullSafeHashCode(direction);
        result += 31 * ObjectUtils.nullSafeHashCode(type);
        result += 31 * ObjectUtils.nullSafeHashCode(weight);
        return result;
    }

    @Override
    public String toString() {
        return String.format("IndexField [ key: %s, direction: %s, type: %s, weight: %s]", key, direction, type, weight);
    }


}
