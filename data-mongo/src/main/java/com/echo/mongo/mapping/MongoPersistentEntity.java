package com.echo.mongo.mapping;

import com.echo.common.convert.core.TypeDescriptor;
import com.echo.mongo.convert.EntityWriter;
import com.echo.mongo.index.PropertyHandler;
import com.echo.mongo.query.Query;
import org.bson.Document;

import java.lang.annotation.Annotation;

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

    <A extends Annotation> boolean isAnnotationPresent(Class<A> annotationType);

    <A extends Annotation> A findAnnotation(Class<A> annotationType);


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


    <T> Document toMappingDocument(T objectToSave, EntityWriter<T> writer);


    /**
     * Returns the identifier of the entity.
     *
     * @return
     */
    Object getId(Object object);

    /**
     * Returns the {@link Query} to find the entity by its identifier.
     *
     * @return
     */
    Query getByIdQuery(Object object);

    /**
     * Returns the {@link Query} to remove an entity by its {@literal id}
     *
     * @return the {@link Query} to use for removing the entity. Never {@literal null}.
     * @since 2.2
     */
    default Query getRemoveByQuery(Object object) {
        return getByIdQuery(object);
    }

    /**
     * Applies the given {@link PropertyHandler} to all {@link MongoPersistentProperty}s contained in this
     * {@link MongoPersistentEntity}. The iteration order is undefined.
     *
     * @param handler must not be {@literal null}.
     */
    void doWithProperties(PropertyHandler handler);
}
