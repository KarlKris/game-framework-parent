package com.echo.mongo.convert;

import org.bson.conversions.Bson;

/**
 * Interface to write objects into store specific sinks.
 * @param <E> the entity type the converter can handle
 */
public interface EntityWriter<E> {


    void write(E source, Bson sink);

}
