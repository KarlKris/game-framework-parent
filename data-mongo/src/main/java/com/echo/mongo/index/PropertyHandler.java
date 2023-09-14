package com.echo.mongo.index;

import com.echo.mongo.mapping.MongoPersistentProperty;

public interface PropertyHandler {

    void doWithPersistentProperty(MongoPersistentProperty property);
}
