package com.echo.autoconfigure.resources;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.NamedThreadFactory;
import com.echo.common.resource.storage.ResourceStorage;
import com.echo.common.resource.storage.StorageManager;
import com.echo.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 资源自动reload
 *
 * @author li-yuanwen
 * @date 2022/9/21
 */
@Slf4j
public class ResourceAutoReload implements Runnable {

    private final String resourcePath;
    private final StorageManager storageManager;

    private WatchService watcher;
    private ExecutorService executorService;

    private volatile boolean stop = false;

    public ResourceAutoReload(String resourcePath, StorageManager storageManager) {
        this.resourcePath = resourcePath;
        this.storageManager = storageManager;
    }

    @PostConstruct
    private void initialize() throws IOException {
        // 初始化监听器
        initWatcher();
        // 启动监听器
        startWatcher();
    }

    @PreDestroy
    private void destroy() {
        stop = true;
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    private void initWatcher() throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        registerFile(FileUtil.file(resourcePath));
    }

    private void registerFile(File file) throws IOException {
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                Paths.get(f.getPath()).register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
                registerFile(f);
            }
        }
    }

    private void startWatcher() {
        executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("资源Reload线程-", false));
        executorService.submit(this);
    }

    private String getClassSimpleName(String filename) {
        if (!filename.endsWith(".xlsx") && !filename.endsWith(".xls")) {
            return null;
        }
        int last = filename.lastIndexOf('.');
        return filename.substring(0, last);
    }

    @Override
    public void run() {
        while (!stop) {
            WatchKey watchKey = null;
            try {
                watchKey = watcher.take();

                List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                List<ResourceStorage<?, ?>> storages = new LinkedList<>();
                for (WatchEvent<?> event : watchEvents) {
                    WatchEvent<Path> e = (WatchEvent<Path>) event;
                    Path path = e.context();
                    String fileName = path.toFile().getName();
                    String classSimpleName = getClassSimpleName(fileName);
                    if (!StringUtils.hasLength(classSimpleName) || StringUtils.startsWithIgnoreCase(fileName, "~")) {
                        continue;
                    }

                    log.error("更新的文件名是: {}", classSimpleName);
                    ResourceStorage<?, ?> storage = storageManager.getResourceStorage(classSimpleName);
                    if (storage == null) {
                        log.error("不是服务端表,忽略: {}", classSimpleName);
                    } else {
                        storage.load();
                        storages.add(storage);
                    }
                }
                // 校验
                boolean validate = true;
                for (ResourceStorage<?, ?> storage : storages) {
                    try {
                        storage.validate(storageManager);
                    } catch (Exception e) {
                        validate = false;
                        log.error(e.getMessage(), e);
                        break;
                    }
                }
                if (!validate) {
                    // 一个校验失败,全部还原
                    storages.forEach(ResourceStorage::validateFailure);
                    return;
                }
                for (ResourceStorage<?, ?> storage : storages) {
                    storage.validateSuccessfully();
                    log.error("更新成功,文件名是: {}", storage.getLocation());
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                if (watchKey != null) {
                    watchKey.reset();
                }
            }
        }
    }
}
