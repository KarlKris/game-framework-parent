package com.echo.common.resource;

import com.echo.common.resource.anno.ResourceForeignKey;
import com.echo.common.util.ReflectionUtils;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 资源定义信息
 * @author li-yuanwen
 * @date 2022/3/16
 */
@Getter
public class ResourceDefinition {

    /** 资源对象 **/
    private final Class<?> clz;
    /** 资源根路径 **/
    private final String rootPath;
    /** 是否有外键 **/
    private final List<Field> foreignKeyFields;

    public ResourceDefinition(Class<?> clz, String rootPath) {
        this.clz = clz;
        this.rootPath = rootPath;
        this.foreignKeyFields = new LinkedList<>();
        foreignKeyFields.addAll(Arrays.asList(ReflectionUtils.getFields(clz, field -> field.getAnnotation(ResourceForeignKey.class) != null)));
    }

    public boolean haveForeignKey() {
        return !foreignKeyFields.isEmpty();
    }
}
