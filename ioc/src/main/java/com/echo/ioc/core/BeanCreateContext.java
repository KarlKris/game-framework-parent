package com.echo.ioc.core;

import java.lang.reflect.Field;

/**
 * 构建bean所需的上下文信息
 */
public interface BeanCreateContext {

    /**
     * 注入bean的field
     * @return /
     */
    Field getField();

}
