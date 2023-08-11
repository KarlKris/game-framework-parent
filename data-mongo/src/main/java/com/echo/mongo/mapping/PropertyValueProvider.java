package com.echo.mongo.mapping;

public interface PropertyValueProvider {

    /**
     * Returns a value for the given {@link MongoPersistentProperty}.
     *
     * @param property will never be {@literal null}.
     * @return
     */
    Object getPropertyValue(MongoPersistentProperty property);


}
