package com.echo.common.resource.resolver;

import com.echo.common.resource.anno.ResourceIndex;

import java.lang.reflect.Method;

/**
 * 基于方法返回值的索引解析器
 * @author li-yuanwen
 * @date 2022/3/22
 */
public class MethodIndexResolver extends MethodResolver implements IndexResolver {

    /** 索引名称 **/
    private final String indexName;
    /** 是否是唯一索引 **/
    private final boolean unique;

    MethodIndexResolver(Method method) {
        super(method);
        ResourceIndex annotation = method.getAnnotation(ResourceIndex.class);
        this.indexName = annotation.indexName();
        this.unique = annotation.uniqueIndex();
    }

    @Override
    public String getIndexName() {
        return indexName;
    }

    @Override
    public boolean isUnique() {
        return unique;
    }
}
