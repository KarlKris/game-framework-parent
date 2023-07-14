package com.echo.common.resource.storage;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.echo.common.resource.ResourceDefinition;
import com.echo.common.resource.anno.ResourceForeignKey;
import com.echo.common.resource.anno.ResourceObj;
import com.echo.common.resource.reader.ResourceReader;
import com.echo.common.util.StringUtils;
import com.echo.common.resource.resolver.IndexResolver;
import com.echo.common.resource.resolver.Resolver;
import com.echo.common.resource.resolver.ResolverFactory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 默认资源容器
 * @author li-yuanwen
 * @date 2022/3/17
 */
@Slf4j
public class DefaultResourceStorage<K, V> implements ResourceStorage<K, V> {

    /** 初始化标识 **/
    private volatile boolean initialize;
    /** 文件资源信息 **/
    private ResourceDefinition resourceDefinition;
    /** 资源文件全路径 **/
    private String path;
    /** 文件资源读取器 **/
    private ResourceReader resourceReader;
    /** 上个版本数据容器 **/
    private InnerValueHolder lastData;
    /** 数据容器 **/
    private InnerValueHolder data;
    /** 数据变更监听器 **/
    private final List<StorageChangeListener> listeners = new LinkedList<>();
    /** 索引解析器 **/
    private List<IndexResolver> indexResolvers;
    /** id解析器 **/
    private Resolver identifier;

    /** 初始化 **/
    public void initialize(ResourceDefinition resourceDefinition, ResourceReader resourceReader) {
        if (initialize) {
            return;
        }
        initialize = true;
        this.resourceDefinition = resourceDefinition;
        this.resourceReader = resourceReader;
        this.path = resolvePath();
        this.identifier = ResolverFactory.createIdResolver(resourceDefinition.getClz());
        this.indexResolvers = ResolverFactory.createIndexResolvers(resourceDefinition.getClz());

        load();
    }

    @Override
    public V getResource(K id) {
        return data.values.get(id);
    }

    @Override
    public V getUniqueResource(String uniqueName, Object uniqueKey) {
        Map<Object, V> map = data.uniqueIndexValues.get(uniqueName);
        if (map == null) {
            return null;
        }
        return map.get(uniqueKey);
    }

    @Override
    public List<V> getIndexResources(String indexName, Object indexKey) {
        Map<Object, List<V>> map = data.indexValues.get(indexName);
        if (map == null) {
            return Collections.emptyList();
        }
        return map.getOrDefault(indexKey, Collections.emptyList());
    }

    @Override
    public Collection<V> getAll() {
        return Collections.unmodifiableCollection(data.values.values());
    }

    @Override
    public void load() {
        load0();
    }

    @Override
    public void validate(StorageManager storageManager) {
       if (!resourceDefinition.haveForeignKey()) {
           return;
       }
       // 校验外键
       resourceDefinition.getForeignKeyFields().forEach(field -> foreignKeyValidate0(getClz(), field, storageManager));
    }

    @Override
    public void validateSuccessfully() {
        // 清空上个版本数据
        this.lastData = null;
        // 通知监听器
        notifyChange();
    }

    @Override
    public void validateFailure() {
        // 还原到上个版本
        this.data = this.lastData;
        this.lastData = null;
    }

    @Override
    public void addListener(StorageChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public String getLocation() {
        return path;
    }

    // -------------- 私有方法 --------------------------------

    /** 文件资源数据加载 **/
    private void load0() {
        InputStream input = null;
        try {
            input = FileUtil.getInputStream(path);
            List<V> list = resourceReader.read(input, getClz());

            InnerValueHolder data = new InnerValueHolder(list.size());
            for (V item : list) {
                if ((item instanceof ResourceValidate) && !((ResourceValidate) item).isValid()) {
                    String message = MessageFormatter.arrayFormat("资源类Class:{} id:{} 数据不合法"
                            , new Object[]{getClz().getName(), identifier.resolve(item)}).getMessage();
                    throw new RuntimeException(message);
                }
                if (data.put(item) != null) {
                    String message = MessageFormatter.arrayFormat("资源类Class:{} 唯一标识：{} 重复"
                            , new Object[]{getClz().getName(), identifier.resolve(item)}).getMessage();
                    throw new RuntimeException(message);
                }
            }
            this.lastData = this.data;
            this.data = data;

        } catch (IORuntimeException e) {
            String message = MessageFormatter.format("资源类Class:[{}]所对应的资源文件[{}]不存在"
                    , getClz().getName(), path).getMessage();
            throw new IllegalStateException(message, e);
        } catch (Exception e){
            String message = MessageFormatter.format("加载资源类Class:[{}]所对应的资源文件[{}]出现未知异常"
                    , getClz().getName(), path).getMessage();
            throw new IllegalStateException(message, e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** 资源类Class **/
    private Class<V> getClz() {
        return (Class<V>) resourceDefinition.getClz();
    }

    /** 解析文件路径 **/
    private String resolvePath() {
        ResourceObj obj = AnnotationUtil.getAnnotation(resourceDefinition.getClz(), ResourceObj.class);
        assert obj != null;
        StringBuilder stringBuilder = new StringBuilder(FILE_PATH);
        if (StringUtils.hasLength(obj.value())) {
            stringBuilder.append(obj.value()).append(FILE_PATH);
        }
        stringBuilder.append(resourceDefinition.getClz().getSimpleName()).append(FILE_SPLIT).append(resourceReader.getFileSuffix());
        return resourceDefinition.getRootPath() + stringBuilder;
    }

    /** 触发监听器 **/
    private void notifyChange() {
        for (StorageChangeListener listener : listeners) {
            listener.notifyChange(this);
        }
    }

    /** 外键验证 **/
    private void foreignKeyValidate0(Class<?> clz, Field field, StorageManager storageManager) {
        Resolver resolver = ResolverFactory.createFieldResolver(field);

        ResourceForeignKey annotation = field.getAnnotation(ResourceForeignKey.class);
        Field foreignKeyField = null;
        try {
            foreignKeyField = annotation.foreignKeyClz().getDeclaredField(annotation.foreignKeyFieldName());
        } catch (NoSuchFieldException e) {
            String message = MessageFormatter.arrayFormat("资源类Class:{} 属性名:{} 所对应的外键类Class:{} 属性名:{} 不存在"
                            , new Object[]{clz.getName(), field.getName(), annotation.foreignKeyClz().getName(), annotation.foreignKeyFieldName()})
                    .getMessage();
            throw new RuntimeException(message);
        }
        Resolver foreignKeyResolver = ResolverFactory.createFieldResolver(foreignKeyField);
        ResourceStorage<?, ?> foreignKeyStorage = storageManager.getResourceStorage(annotation.foreignKeyClz());
        Collection<?> list = foreignKeyStorage.getAll();
        Set<Object> foreignKeyValueSet = new HashSet<>(list.size());
        for (Object obj : list) {
            foreignKeyValueSet.add(foreignKeyResolver.resolve(obj));
        }
        ResourceStorage<?, ?> storage = storageManager.getResourceStorage(clz);
        for (Object obj : storage.getAll()) {
            Object value = resolver.resolve(obj);
            if (!foreignKeyValueSet.contains(value)) {
                String message = MessageFormatter.arrayFormat("资源类Class:{} 属性名:{} 值:{} 所对应的外键类Class:{} 属性名:{} 中不存在"
                                , new Object[]{clz.getName(), field.getName(), value, annotation.foreignKeyClz().getName(), annotation.foreignKeyFieldName()})
                        .getMessage();
                throw new RuntimeException(message);
            }
        }
    }

    /** 资源数据容器 **/
    private final class InnerValueHolder {

        /** 主数据存储空间 **/
        private Map<K, V> values;
        /** 索引数据存储空间 **/
        private Map<String, Map<Object, List<V>>> indexValues;
        /** 唯一索引数据存储空间 **/
        private Map<String, Map<Object, V>> uniqueIndexValues;

        InnerValueHolder(int size) {
            this.values = new HashMap<>(size);
            this.indexValues = new HashMap<>(2);
            this.uniqueIndexValues = new HashMap<>(2);
        }

        private V put(V item) {
            @SuppressWarnings("unchecked")
            K id = (K) identifier.resolve(item);
            V old = values.put(id, item);
            // 索引处理
            indexResolvers.forEach(resolver -> processIndexResolver(resolver, item));
            return old;
        }

        private void processIndexResolver(IndexResolver resolver, V item) {
            String indexName = resolver.getIndexName();
            Object indexKey = resolver.resolve(item);
            if (resolver.isUnique()) {
                Map<Object, V> map = loadUniqueIndex(indexName);
                if (map.put(indexKey, item) != null) {
                    String message = MessageFormatter.arrayFormat("资源:{} 唯一索引名称:{} 值:{} 重复"
                            , new Object[]{getClz().getName(), indexName, indexKey}).getMessage();
                    throw new RuntimeException(message);
                }
            } else {
                Map<Object, List<V>> map = loadIndex(indexName);
                // indexKey 不可为数组,集合
                List<V> list = map.computeIfAbsent(indexKey, k -> new LinkedList<>());
                list.add(item);
            }
        }

        private Map<Object, V> loadUniqueIndex(String uniqueIndexName) {
            return this.uniqueIndexValues.computeIfAbsent(uniqueIndexName, k -> new HashMap<>(64));
        }

        private Map<Object, List<V>> loadIndex(String indexName) {
            return this.indexValues.computeIfAbsent(indexName, k -> new HashMap<>(64));
        }
    }
}
