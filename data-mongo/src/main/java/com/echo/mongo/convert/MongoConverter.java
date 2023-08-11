package com.echo.mongo.convert;

import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.convert.exception.ConversionException;
import com.echo.common.util.ClassUtils;
import com.echo.mongo.CodecRegistryProvider;
import com.echo.mongo.mapping.MongoPersistentEntity;
import com.mongodb.MongoClientSettings;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

/**
 * Central Mongo specific converter interface which combines {@link EntityWriter} and {@link EntityReader}.
 */
public interface MongoConverter extends EntityWriter<Object>, EntityReader<Object>, CodecRegistryProvider {


    default Object convertToMongoType(Object obj) {
        return convertToMongoType(obj, (TypeDescriptor) null);
    }

    default Object convertToMongoType(Object obj, MongoPersistentEntity entity) {
        return convertToMongoType(obj, entity.getTypeDescriptor());
    }

    Object convertToMongoType(Object obj, TypeDescriptor typeDescriptor);



    /**
     * Converts the given raw id value into either {@link ObjectId} or {@link String}.
     *
     * @param id can be {@literal null}.
     * @param targetType must not be {@literal null}.
     * @return {@literal null} if source {@literal id} is already {@literal null}.
     * @since 2.2
     */
    default Object convertId(Object id, Class<?> targetType) {

        if (id == null || ClassUtils.isAssignableValue(targetType, id)) {
            return id;
        }

        if (ClassUtils.isAssignable(ObjectId.class, targetType)) {

            if (id instanceof String) {

                if (ObjectId.isValid(id.toString())) {
                    return new ObjectId(id.toString());
                }

                // avoid ConversionException as convertToMongoType will return String anyways.
                return id;
            }
        }

        try {
            return getConversionService().canConvert(id.getClass(), targetType)
                    ? getConversionService().convert(id, targetType)
                    : convertToMongoType(id, (TypeDescriptor) null);
        } catch (ConversionException o_O) {
            return convertToMongoType(id, (TypeDescriptor) null);
        }
    }


    ConversionService getConversionService();

    @Override
    default CodecRegistry getCodecRegistry() {
        return MongoClientSettings.getDefaultCodecRegistry();
    }

}
