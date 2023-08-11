package com.echo.mongo.mapping;

import com.echo.common.convert.core.TypeDescriptor;

/**
 * mongo 持久化实体
 * @author: li-yuanwen
 */
public interface MongoPersistentEntity extends Iterable<MongoPersistentProperty> {


    /**
     * @return document 名称
     */
    String getCollectionName();


    TypeDescriptor getTypeDescriptor();


    void addPersistentProperty(MongoPersistentProperty property);

    MongoPersistentProperty getPersistentProperty(String name);

    /**
     * Returns whether the {@link MongoPersistentEntity} has an id property. If this call returns {@literal true},
     * {@link #getIdProperty()} will return a non-{@literal null} value.
     *
     * @return {@literal true} if entity has an {@literal id} property.
     */
    boolean hasIdProperty();

    MongoPersistentProperty getIdProperty();

    /**
     * Returns the id property of the {@link MongoPersistentEntity}.
     *
     * @return the id property of the {@link MongoPersistentEntity}.
     * @throws IllegalStateException if {@link MongoPersistentEntity} does not define an {@literal id} property.
     * @since 2.0
     */
    default MongoPersistentProperty getRequiredIdProperty() {

        MongoPersistentProperty property = getIdProperty();

        if (property != null) {
            return property;
        }

        throw new IllegalStateException(String.format("Required identifier property not found for %s", getType()));
    }

    Class<?> getType();

    /**
     * Returns whether the given {@link MongoPersistentEntity} is the id property of the entity.
     *
     * @param property can be {@literal null}.
     * @return {@literal true} given property is the entities id.
     */
    boolean isIdProperty(MongoPersistentProperty property);

}
