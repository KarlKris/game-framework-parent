package com.echo.mongo.index;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface CompoundIndexes {

    CompoundIndex[] value();

}
