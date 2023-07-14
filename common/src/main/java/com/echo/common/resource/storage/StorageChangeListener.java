package com.echo.common.resource.storage;

/**
 * Storage变更监听器
 * @author li-yuanwen
 * @date 2022/3/17
 */
public interface StorageChangeListener {

    /**
     * 变更通知
     */
    void notifyChange(ResourceStorage<?, ?> storage);

}
