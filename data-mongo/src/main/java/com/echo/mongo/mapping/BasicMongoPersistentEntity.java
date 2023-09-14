package com.echo.mongo.mapping;

import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.StringUtils;
import com.echo.mongo.convert.EntityWriter;
import com.echo.mongo.excetion.MappingException;
import com.echo.mongo.index.PropertyHandler;
import com.echo.mongo.query.Criteria;
import com.echo.mongo.query.Query;
import org.bson.Document;

import java.lang.annotation.Annotation;
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
        com.echo.mongo.mapping.anno.Document annotation = descriptor.getAnnotation(com.echo.mongo.mapping.anno.Document.class);
        if (annotation != null) {
            this.collection = StringUtils.hasLength(annotation.collection()) ? annotation.collection()
                    : getPreferredCollectionName(descriptor.getType());
        } else {
            this.collection = getPreferredCollectionName(descriptor.getType());
        }
    }

    @Override
    public <A extends Annotation> boolean isAnnotationPresent(Class<A> annotationType) {
        return descriptor.hasAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A findAnnotation(Class<A> annotationType) {
        return descriptor.getAnnotation(annotationType);
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

    @Override
    public <T> Document toMappingDocument(T objectToSave, EntityWriter<T> writer) {
        Document document = new Document();
        writer.write(objectToSave, document);

        String idField = MongoPersistentProperty.ID_FIELD_NAME;
        if (document.containsKey(idField) && document.get(idField) == null) {
            document.remove(idField);
        }

        return document;
    }

    @Override
    public Object getId(Object object) {
        PersistentPropertyAccessor<?> accessor = new InstanceWrapper<>(object);
        return accessor.getProperty(getRequiredIdProperty());
    }

    @Override
    public Query getByIdQuery(Object object) {
        if (!hasIdProperty()) {
            throw new MappingException("No id property found for object of type " + getType());
        }

        MongoPersistentProperty idProperty = getRequiredIdProperty();

        return Query.query(Criteria.where(idProperty.getName()).is(getId(object)));
    }

    public void doWithProperties(PropertyHandler handler) {

        if (handler == null) {
            throw new IllegalArgumentException("PropertyHandler must not be null");
        }

        for (MongoPersistentProperty property : propertyCache.values()) {
            handler.doWithPersistentProperty(property);
        }
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
