package com.li.ioc.core;

/**
 * bean工厂
 * @author li-yuanwen
 * @date 2023/03/17
 * @param <T>
 */
public interface ObjectFactory<T> {

    /**
     * @return bean
     */
    T getObject();

}
