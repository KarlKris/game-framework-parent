package com.echo.mongo.mapping;

import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.Optionals;
import com.echo.common.util.ReflectionUtils;
import com.echo.common.util.SimpleTypeHolder;
import com.echo.common.util.TypeDescriptorUtils;
import com.echo.mongo.excetion.MappingException;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * mongodb 映射上下文
 *
 * @author: li-yuanwen
 */
public class MongoMappingContext {

    private static final Optional<MongoPersistentEntity> NONE = Optional.empty();
    private SimpleTypeHolder simpleTypeHolder = MongoSimpleTypes.HOLDER;

    /** mongodb映射的实体类型集 **/
    private ManagedTypes managedTypes = ManagedTypes.empty();

    private final Map<TypeDescriptor, Optional<MongoPersistentEntity>> persistentEntities = new HashMap<>();

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * Whether to enable auto-index creation.
     */
    private boolean autoIndexCreation = false;

    public void setSimpleTypeHolder(SimpleTypeHolder simpleTypeHolder) {
        this.simpleTypeHolder = simpleTypeHolder;
    }

    public void setManagedTypes(ManagedTypes managedTypes) {
        this.managedTypes = managedTypes;
    }

    public void setAutoIndexCreation(boolean autoIndexCreation) {
        this.autoIndexCreation = autoIndexCreation;
    }

    public boolean isAutoIndexCreation() {
        return autoIndexCreation;
    }

    public void initialize() {
        managedTypes.forEach(this::addPersistentEntity);
    }

    /**
     * Adds the given type to the {@link MongoMappingContext}.
     *
     * @param type must not be {@literal null}.
     * @return
     */
    protected MongoPersistentEntity addPersistentEntity(Class<?> type) {
        return addPersistentEntity(TypeDescriptor.valueOf(type));
    }

    /**
     * Adds the given {@link TypeDescriptor} to the {@link MongoMappingContext}.
     *
     * @param typeDescriptor must not be {@literal null}.
     * @return
     */
    protected MongoPersistentEntity addPersistentEntity(TypeDescriptor typeDescriptor) {

        if (typeDescriptor == null) {
            throw new IllegalArgumentException("TypeInformation must not be null");
        }

        try {

            readWriteLock.readLock().lock();

            Optional<MongoPersistentEntity> optional = persistentEntities.get(typeDescriptor);

            if (optional != null) {
                return optional.orElse(null);
            }

        } finally {
            readWriteLock.readLock().unlock();
        }

        MongoPersistentEntity entity;

        try {

            readWriteLock.writeLock().lock();

            Optional<MongoPersistentEntity> optional = persistentEntities.get(typeDescriptor);

            if (optional != null) {
                return optional.orElse(null);
            }

            entity = doAddPersistentEntity(typeDescriptor);

        } catch (Exception e) {
            throw new MappingException(e.getMessage(), e);
        } finally {
            readWriteLock.writeLock().unlock();
        }

        return entity;
    }

    private MongoPersistentEntity doAddPersistentEntity(TypeDescriptor typeDescriptor) {

        try {

            Class<?> type = typeDescriptor.getType();

            MongoPersistentEntity entity = createPersistentEntity(typeDescriptor);

            // Eagerly cache the entity as we might have to find it during recursive lookups.
            persistentEntities.put(typeDescriptor, Optional.of(entity));

            for (Field field : ReflectionUtils.getFields(type)) {
                createAndRegisterProperty(entity, field);
            }
            return entity;

        } catch (RuntimeException e) {
            persistentEntities.remove(typeDescriptor);
            throw e;
        }
    }


    private void createAndRegisterProperty(MongoPersistentEntity entity, Field field) {
        MongoPersistentProperty property = new MongoPersistentProperty(entity, field);
        if (property.isTransient()) {
            return;
        }
        entity.addPersistentProperty(property);

        TypeDescriptor fieldTypeDescriptor = TypeDescriptorUtils.newInstance(field);
        if (fieldTypeDescriptor.isMap()) {
            TypeDescriptor keyTypeDescriptor = fieldTypeDescriptor.getMapKeyTypeDescriptor();
            if (shouldCreatePersistentEntityFor(keyTypeDescriptor)) {
                addPersistentEntity(keyTypeDescriptor);
            }
            TypeDescriptor valueTypeDescriptor = fieldTypeDescriptor.getMapValueTypeDescriptor();
            if (shouldCreatePersistentEntityFor(valueTypeDescriptor)) {
                addPersistentEntity(valueTypeDescriptor);
            }
        } else if (fieldTypeDescriptor.isCollectionLike()) {
            TypeDescriptor elementTypeDescriptor = fieldTypeDescriptor.getElementTypeDescriptor();
            if (shouldCreatePersistentEntityFor(elementTypeDescriptor)) {
                addPersistentEntity(elementTypeDescriptor);
            }
        } else {
            TypeDescriptor typeDescriptor = TypeDescriptor.valueOf(fieldTypeDescriptor.getType());
            if (shouldCreatePersistentEntityFor(typeDescriptor)) {
                addPersistentEntity(typeDescriptor);
            }
        }

    }


    public MongoPersistentEntity getPersistentEntity(Class<?> type) {
        return getPersistentEntity(TypeDescriptor.valueOf(type));
    }

    public MongoPersistentEntity getPersistentEntity(TypeDescriptor typeDescriptor) {
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();

        try {
            readLock.lock();
            Optional<MongoPersistentEntity> optional = persistentEntities.get(typeDescriptor);
            if (optional != null) {
                return optional.orElse(null);
            }
        } finally {
            readLock.unlock();
        }

        if (!shouldCreatePersistentEntityFor(typeDescriptor)) {
            ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
            try {
                writeLock.lock();
                persistentEntities.put(typeDescriptor, NONE);
            } finally {
                writeLock.unlock();
            }

            return null;
        }

        return addPersistentEntity(typeDescriptor);
    }

    /**
     * Returns a required {@link MongoPersistentEntity} for the given {@link Class}. Will throw
     * {@link IllegalArgumentException} for types that are considered simple ones.
     *
     * @see SimpleTypeHolder#isSimpleType(Class)
     * @param type must not be {@literal null}.
     * @return never {@literal null}.
     * @throws MappingException when no {@link MongoPersistentEntity} can be found for given {@literal type}.
     * @since 2.0
     */
    public MongoPersistentEntity getRequiredPersistentEntity(Class<?> type) throws MappingException {

        MongoPersistentEntity entity = getPersistentEntity(type);

        if (entity != null) {
            return entity;
        }

        throw new MappingException(String.format("Couldn't find PersistentEntity for type %s", type));
    }

    protected BasicMongoPersistentEntity createPersistentEntity(TypeDescriptor typeDescriptor) {
        return new BasicMongoPersistentEntity(typeDescriptor);
    }

    /**
     * Returns whether a {@link MongoPersistentEntity} instance should be created for the given {@link TypeDescriptor}. By
     * default this will reject all types considered simple, but it might be necessary to
     * tweak that in case you have registered custom converters for top level types (which renders them to be considered
     * simple) but still need meta-information about them.
     *
     * @param descriptor will never be {@literal null}.
     * @return
     */
    protected boolean shouldCreatePersistentEntityFor(TypeDescriptor descriptor) {
        if (simpleTypeHolder.isSimpleType(descriptor.getType())) {
            return false;
        }

        return !Optional.class.isAssignableFrom(descriptor.getType());
    }

    public Collection<MongoPersistentEntity> getPersistentEntities() {
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        try {

            readLock.lock();

            return persistentEntities.values().stream()//
                    .flatMap(Optionals::toStream)//
                    .collect(Collectors.toSet());

        } finally {
            readLock.unlock();
        }
    }
}
