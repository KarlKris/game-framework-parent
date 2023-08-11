package com.echo.mongo.mapping;

/**
 * Domain service to allow accessing and setting PersistentPropertys of an entity.
 * Usually obtained through PersistentEntity.getPropertyAccessor(Object).
 * In case type conversion shall be applied on property access, use a ConvertingPropertyAccessor.
 * This service supports mutation for immutable classes by creating new object instances.
 * These are managed as state of PersistentPropertyAccessor and must be obtained from getBean() after processing all updates.
 */
public interface PersistentPropertyAccessor<T> {

    /**
     * Sets the given {@link MongoPersistentProperty} to the given value. Will do type conversion if a
     * {@link com.echo.common.convert.core.ConversionService} is configured.
     *
     * @param property must not be {@literal null}.
     * @param value can be {@literal null}.
     * @throws com.echo.mongo.excetion.MappingException in case an exception occurred when setting the property value.
     */
    void setProperty(MongoPersistentProperty property, Object value);

    /**
     * Returns the value of the given {@link MongoPersistentProperty} of the underlying bean instance.
     *
     * @param property must not be {@literal null}.
     * @return can be {@literal null}.
     */
    Object getProperty(MongoPersistentProperty property);

    /**
     * Returns the underlying bean. The actual instance may change between
     * {@link #setProperty(MongoPersistentProperty, Object)} calls.
     *
     * @return will never be {@literal null}.
     */
    T getBean();

}
