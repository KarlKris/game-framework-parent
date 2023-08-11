package com.echo.mongo.mapping;

import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.util.ReflectionUtils;
import com.echo.common.util.SimpleTypeHolder;
import com.echo.mongo.excetion.MappingException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * mongodb 映射上下文
 * @author: li-yuanwen
 */
public class MongoMappingContext {


    private final SimpleTypeHolder simpleTypeHolder = MongoSimpleTypes.HOLDER;

    /** mongodb映射的实体类型集 **/
    private ManagedTypes managedTypes = ManagedTypes.empty();

    private final Map<TypeDescriptor, MongoPersistentEntity> persistentEntities = new HashMap<>();

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public void setManagedTypes(ManagedTypes managedTypes) {
        this.managedTypes = managedTypes;
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

            MongoPersistentEntity persistentEntity = persistentEntities.get(typeDescriptor);

            if (persistentEntity != null) {
                return persistentEntity;
            }

        } finally {
            readWriteLock.readLock().unlock();
        }

        MongoPersistentEntity entity;

        try {

            readWriteLock.writeLock().lock();

            MongoPersistentEntity persistentEntity = persistentEntities.get(typeDescriptor);

            if (persistentEntity != null) {
                return persistentEntity;
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
            persistentEntities.put(typeDescriptor, entity);

            for (Field field : ReflectionUtils.getFields(type)) {
                MongoPersistentProperty property = new MongoPersistentProperty(entity, field);
                if (property.isTransient()) {
                    continue;
                }
                entity.addPersistentProperty(property);
            }

            return entity;

        } catch (RuntimeException e) {
            persistentEntities.remove(typeDescriptor);
            throw e;
        }
    }



    public MongoPersistentEntity getPersistentEntity(Class<?> type) {
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();

        try {
            readLock.lock();
            return persistentEntities.get(TypeDescriptor.valueOf(type));
        } finally {
            readLock.unlock();
        }
    }

    public MongoPersistentEntity getPersistentEntity(TypeDescriptor typeDescriptor) {
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();

        try {
            readLock.lock();
            return persistentEntities.get(typeDescriptor);
        } finally {
            readLock.unlock();
        }
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

}
