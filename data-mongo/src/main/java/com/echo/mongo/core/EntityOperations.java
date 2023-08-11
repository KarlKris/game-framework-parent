package com.echo.mongo.core;

import com.echo.common.convert.core.TypeDescriptor;
import com.echo.mongo.excetion.InvalidMongoDbApiUsageException;
import com.echo.mongo.excetion.MappingException;
import com.echo.mongo.mapping.MongoMappingContext;
import com.echo.mongo.mapping.MongoPersistentEntity;

/**
 * Entity 相关操作
 * @author: li-yuanwen
 */
public class EntityOperations {

    private static final String ID_FIELD = "_id";

    private final MongoMappingContext context;

    public EntityOperations(MongoMappingContext context) {
        this.context = context;
    }

    public String determineCollectionName(Class<?> entityClass) {
        return getPersistentEntity(entityClass).getCollectionName();
    }

    public MongoPersistentEntity getPersistentEntity(Class<?> entityClass) {
        if (entityClass == null) {
            throw new InvalidMongoDbApiUsageException(
                    "No class parameter provided, entity collection can't be determined");
        }
        MongoPersistentEntity persistentEntity = context.getPersistentEntity(TypeDescriptor.valueOf(entityClass));
        if (persistentEntity == null) {
            throw new MappingException(String.format(
                    "Cannot determine collection name from type '%s'. Is it a store native type?", entityClass.getName()));
        }
        return persistentEntity;
    }
}
