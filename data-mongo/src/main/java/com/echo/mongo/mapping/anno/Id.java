package com.echo.mongo.mapping.anno;

import java.lang.annotation.*;

/**
 * 标注document中的_id字段
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface Id {


}
