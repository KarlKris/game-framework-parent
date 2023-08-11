package com.echo.mongo.mapping;

import cn.hutool.core.lang.Assert;
import com.echo.common.util.ReflectionUtils;
import com.echo.mongo.excetion.MappingException;

import java.lang.reflect.Field;

/**
 * Domain service to allow accessing the values of MongoPersistentProperty on a given bean.
 * @author: li-yuanwen
 */
public class InstanceWrapper<T> implements PersistentPropertyAccessor<T> {

    private final T bean;

    public InstanceWrapper(T bean) {
        this.bean = bean;
    }

    @SuppressWarnings("unchecked")
    public void setProperty(MongoPersistentProperty property, Object value) {

        Assert.notNull(property, "MongoPersistentProperty must not be null");

        try {

            Field field = property.getField();

            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, bean, value);
        } catch (IllegalStateException e) {
            throw new MappingException("Could not set object property", e);
        }
    }

    public Object getProperty(MongoPersistentProperty property) {
        return getProperty(property, property.getActualType());
    }

    /**
     * Returns the value of the given {@link MongoPersistentProperty} potentially converted to the given type.
     *
     * @param <S>
     * @param property must not be {@literal null}.
     * @param type can be {@literal null}.
     * @return
     * @throws MappingException in case an exception occured when accessing the property.
     */
    public <S> Object getProperty(MongoPersistentProperty property, Class<? extends S> type) {

        Assert.notNull(property, "PersistentProperty must not be null");

        try {

            Field field = property.getField();

            ReflectionUtils.makeAccessible(field);
            return ReflectionUtils.getField(field, bean);

        } catch (IllegalStateException e) {
            throw new MappingException(
                    String.format("Could not read property %s of %s", property.toString(), bean.toString()), e);
        }
    }

    @Override
    public T getBean() {
        return bean;
    }
}
