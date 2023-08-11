package com.echo.mongo.convert;

import org.bson.conversions.Bson;

/**
 * Interface to read object from store specific sources.
 * @author: li-yuanwen
 */
public interface EntityReader<E> {


    <T extends E> T read(Class<T> type, Bson source);

}
