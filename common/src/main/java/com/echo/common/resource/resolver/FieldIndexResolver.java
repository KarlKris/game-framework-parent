package com.echo.common.resource.resolver;

import com.echo.common.resource.anno.ResourceIndex;

import java.lang.reflect.Field;

/**
 * 基于属性值的索引解析器
 * @author li-yuanwen
 * @date 2022/3/22
 */
public class FieldIndexResolver extends FieldResolver implements IndexResolver {

    /** 索引名称 **/
    private final String indexName;
    /** 是否是唯一索引 **/
    private final boolean uniqueIndex;

    FieldIndexResolver(Field field) {
        super(field);
        ResourceIndex annotation = field.getAnnotation(ResourceIndex.class);
        this.indexName = annotation.indexName();
        this.uniqueIndex = annotation.uniqueIndex();
    }

    @Override
    public String getIndexName() {
        return indexName;
    }

    @Override
    public boolean isUnique() {
        return uniqueIndex;
    }
}
