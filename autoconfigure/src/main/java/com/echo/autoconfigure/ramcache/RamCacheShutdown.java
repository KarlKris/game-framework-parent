package com.echo.autoconfigure.ramcache;

import com.echo.ioc.context.ApplicationCloseListener;
import com.echo.ramcache.entity.DataPersistence;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 内存缓存关闭回写线程池
 *
 * @author: li-yuanwen
 */
@Slf4j
public class RamCacheShutdown implements ApplicationCloseListener {

    private final DataPersistence dataPersistence;

    public RamCacheShutdown(DataPersistence dataPersistence) {
        this.dataPersistence = dataPersistence;
    }

    @Override
    public void applicationClose() {
        Future<?> future = dataPersistence.shutdownGracefully();
        try {
            future.get();
            log.warn("内存缓存回写线程池正常关闭");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
