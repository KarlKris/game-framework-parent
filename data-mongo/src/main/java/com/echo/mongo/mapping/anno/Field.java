package com.echo.mongo.mapping.anno;

import com.echo.mongo.mapping.FieldType;

import java.lang.annotation.*;

/**
 * 标注document中的字段
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface Field {

    /**
     * 映射document中的字段名称,若为空,则使用属性名称作为字段名称
     * @return 映射document中的字段名称
     */
    String name() default "";


    /**
     * The actual desired target type the field should be stored as.
     *
     * @return {@link FieldType#IMPLICIT} by default.
     * @since 2.2
     */
    FieldType targetType() default FieldType.IMPLICIT;

}
