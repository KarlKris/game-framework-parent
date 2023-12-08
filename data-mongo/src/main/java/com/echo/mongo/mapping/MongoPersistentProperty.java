package com.echo.mongo.mapping;


import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.data.Transient;
import com.echo.common.util.StringUtils;
import com.echo.common.util.TypeDescriptorUtils;
import com.echo.mongo.mapping.anno.Id;
import org.bson.types.ObjectId;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * mongodb 持久化属性
 * @author: li-yuanwen
 */
public class MongoPersistentProperty {

    public static final String ID_FIELD_NAME = "_id";

    private static final Set<Class<?>> SUPPORTED_ID_TYPES = new HashSet<Class<?>>();
    private static final Set<String> SUPPORTED_ID_PROPERTY_NAMES = new HashSet<String>();

    static {

        SUPPORTED_ID_TYPES.add(ObjectId.class);
        SUPPORTED_ID_TYPES.add(String.class);
        SUPPORTED_ID_TYPES.add(BigInteger.class);

        SUPPORTED_ID_PROPERTY_NAMES.add("id");
        SUPPORTED_ID_PROPERTY_NAMES.add("_id");
    }

    private final MongoPersistentEntity persistentEntity;
    private final Field field;
    private final TypeDescriptor descriptor;

    private final String name;

    public MongoPersistentProperty(MongoPersistentEntity persistentEntity, Field field) {
        this.persistentEntity = persistentEntity;
        this.field = field;
        this.descriptor = TypeDescriptorUtils.newInstance(field);
        if (hasExplicitFieldName()) {
            this.name = getAnnotatedFieldName();
        } else {
            this.name = StringUtils.lowerFirst(field.getName());
        }
    }

    public Field getField() {
        return field;
    }

    public String getName() {
        return name;
    }

    public TypeDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Returns the name of the field a property is persisted to.
     *
     * @return
     */
    public String getFieldName() {
        if (isIdProperty()) {
            if (getOwner().getIdProperty() == null) {
                return ID_FIELD_NAME;
            }

            if (getOwner().isIdProperty(this)) {
                return ID_FIELD_NAME;
            }
        }
        return getName();
    }

    public MongoPersistentEntity getOwner() {
        return persistentEntity;
    }

    /**
     * Returns the actual type of the property. This will be the original property type if no generics were used, the
     * component type for collection-like types and arrays as well as the value type for map properties.
     *
     * @return
     */
    public Class<?> getActualType() {
        return field.getType();
    }

    /**
     * Returns whether the property is a <em>potential</em> identifier property of the owning {@link MongoPersistentEntity}.
     * This method is mainly used by {@link MongoPersistentEntity} implementation to discover id property candidates on
     * {@link MongoPersistentEntity} creation you should rather call {@link MongoPersistentEntity#isIdProperty(MongoPersistentProperty)}
     * to determine whether the current property is the id property of that {@link MongoPersistentEntity} under consideration.
     *
     * @return {@literal true} if the {@literal id} property.
     */
    public boolean isIdProperty() {
        if (field.getAnnotation(Id.class) != null) {
            return true;
        }
        // We need to support a wider range of ID types than just the ones that can be converted to an ObjectId
        // but still we need to check if there happens to be an explicit name set
        return SUPPORTED_ID_PROPERTY_NAMES.contains(getName()) && !hasExplicitFieldName();
    }

    /**
     * Returns whether the property is a {@link Map}.
     *
     * @return
     */
    public boolean isMap() {
        return descriptor.isMap();
    }

    /**
     * Returns whether the property is an array.
     *
     * @return
     */
    public boolean isArray() {
        return descriptor.isArray();
    }

    /**
     * Returns whether the property is transient.
     *
     * @return
     */
    public boolean isTransient() {
        return field.getAnnotation(Transient.class) != null;
    }

    /**
     * Returns whether the current property is writable, i.e. if the value held for it shall be written to the data store.
     *
     * @return
     * @since 1.9
     */
    public boolean isWritable() {
        return !isTransient();
    }

    /**
     * Returns the {@link Class Java FieldType} of the field a property is persisted to.
     *
     * @return
     */
    public Class<?> getFieldType() {
        return field.getType();
    }

    /**
     * Returns whether the property should be written to the database if its value is {@literal null}.
     *
     * @return
     */
    public boolean writeNullValues() {
        return false;
    }

    /**
     * @return {@literal true} if the property defines an explicit
     * {@link com.echo.mongo.mapping.anno.Field#targetType() target type}.
     * @since 2.2
     */
    public boolean hasExplicitWriteTarget() {
        com.echo.mongo.mapping.anno.Field field = findAnnotation(com.echo.mongo.mapping.anno.Field.class);
        return field != null && !FieldType.IMPLICIT.equals(field.targetType());
    }

    /**
     * @return true if {@link com.echo.mongo.mapping.anno.Field} having non blank
     *         {@link com.echo.mongo.mapping.anno.Field#name()} ()} present.
     * @since 1.7
     */
    public boolean hasExplicitFieldName() {
        return StringUtils.hasLength(getAnnotatedFieldName());
    }

    private String getAnnotatedFieldName() {
        com.echo.mongo.mapping.anno.Field annotation = findAnnotation(com.echo.mongo.mapping.anno.Field.class);
        return annotation != null ? annotation.name() : null;
    }

    public <A extends Annotation> A findAnnotation(Class<A> annotationType) {
        return field.getAnnotation(annotationType);
    }
}
