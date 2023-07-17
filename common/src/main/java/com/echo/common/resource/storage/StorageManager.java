package com.echo.common.resource.storage;

import cn.hutool.core.annotation.AnnotationUtil;
import com.echo.common.resource.ResourceDefinition;
import com.echo.common.resource.anno.ResourceObj;
import com.echo.common.resource.reader.ResourceReader;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 资源容器
 * @author li-yuanwen
 * @date 2022/3/16
 */
@Slf4j
public class StorageManager {


    /** 资源对象信息容器 **/
    private final ConcurrentHashMap<Class<?>, ResourceDefinition> resourceDefinitions = new ConcurrentHashMap<>();
    /** 资源工厂容器 **/
    private final ConcurrentHashMap<Class<?>, ResourceStorage<?, ?>> storages = new ConcurrentHashMap<>();


    public void initialize(ResourceDefinition definition, ResourceReader reader) {
        Class<?> clz = definition.getClz();
        if (resourceDefinitions.putIfAbsent(clz, definition) != null) {
            throw new RuntimeException("资源类[" + clz.getName() + "]ResourceDefinition重复");
        }
        initializeStorage(clz, reader);
    }

    public void validate(ResourceDefinition definition) {
        getResourceStorage(definition.getClz()).validate(this);
    }

    public ResourceStorage<?, ?> getResourceStorage(String clzName) {
        for (Map.Entry<Class<?>, ResourceStorage<?, ?>> entry : storages.entrySet()) {
            if (entry.getKey().getSimpleName().equals(clzName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public ResourceStorage<?, ?> getResourceStorage(Class<?> clz) {
        ResourceStorage<?, ?> storage = storages.get(clz);
        if (storage != null) {
            return storage;
        }

        throw new RuntimeException("资源[" + clz.getName() + "]ResourceStorage不存在");
    }

    private void initializeStorage(Class<?> clz, ResourceReader resourceReader) {
        ResourceDefinition definition = this.resourceDefinitions.get(clz);
        if (definition == null) {
            throw new RuntimeException("资源类[" + clz.getName() + "]的ResourceDefinition不存在");
        }
        ResourceObj obj = AnnotationUtil.getAnnotation(clz, ResourceObj.class);
        assert obj != null;
        DefaultResourceStorage<?, ?> storage = new DefaultResourceStorage<>();
        ResourceStorage<?, ?> prev = storages.putIfAbsent(clz, storage);
        if (prev == null) {
            storage.initialize(definition, resourceReader);
        }
    }
}
