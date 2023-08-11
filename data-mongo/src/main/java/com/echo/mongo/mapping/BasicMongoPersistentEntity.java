package com.echo.mongo.mapping;

import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.StringUtils;
import com.echo.mongo.excetion.MappingException;
import com.echo.mongo.mapping.anno.Document;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * mongodb 持久化实体数据
 * @author: li-yuanwen
 */
public class BasicMongoPersistentEntity implements MongoPersistentEntity {

    private final String collection;
    private final TypeDescriptor descriptor;
    private MongoPersistentProperty idProperty;

    private final Map<String, MongoPersistentProperty> propertyCache;

    public BasicMongoPersistentEntity(TypeDescriptor descriptor) {
        this.descriptor = descriptor;
        this.propertyCache = new HashMap<>(16, 1f);
        Document annotation = descriptor.getAnnotation(Document.class);
        if (annotation != null) {
            this.collection = StringUtils.hasLength(annotation.collection()) ? annotation.collection()
                    : getPreferredCollectionName(descriptor.getType());
        } else {
            this.collection = getPreferredCollectionName(descriptor.getType());
        }
    }

    @Override
    public String getCollectionName() {
        return collection;
    }

    @Override
    public TypeDescriptor getTypeDescriptor() {
        return descriptor;
    }

    @Override
    public void addPersistentProperty(MongoPersistentProperty property) {
        if (propertyCache.containsKey(property.getFieldName())) {
            return;
        }

        propertyCache.putIfAbsent(property.getFieldName(), property);

        MongoPersistentProperty candidate = returnPropertyIfBetterIdPropertyCandidateOrNull(property);
        if (candidate != null) {
            this.idProperty = candidate;
        }

    }

    @Override
    public MongoPersistentProperty getPersistentProperty(String name) {
        return propertyCache.get(name);
    }

    @Override
    public MongoPersistentProperty getIdProperty() {
        return idProperty;
    }

    @Override
    public Class<?> getType() {
        return descriptor.getType();
    }

    @Override
    public boolean isIdProperty(MongoPersistentProperty property) {
        return property.isIdProperty();
    }

    @Override
    public boolean hasIdProperty() {
        return idProperty != null;
    }

    @Override
    public Iterator<MongoPersistentProperty> iterator() {
        Iterator<MongoPersistentProperty> iterator = propertyCache.values().iterator();

        return new Iterator<MongoPersistentProperty>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public MongoPersistentProperty next() {
                return iterator.next();
            }
        };
    }

    protected MongoPersistentProperty returnPropertyIfBetterIdPropertyCandidateOrNull(MongoPersistentProperty property) {

        if (!property.isIdProperty()) {
            return null;
        }

        MongoPersistentProperty idProperty = this.idProperty;

        if (idProperty != null) {
            throw new MappingException(String.format("Attempt to add id property %s but already have property %s registered "
                    + "as id; Check your mapping configuration ", property.getField(), idProperty.getField()));
        }

        return property;
    }

    public static String getPreferredCollectionName(Class<?> entityClass) {
        return StringUtils.lowerFirst(entityClass.getSimpleName());
    }
}
