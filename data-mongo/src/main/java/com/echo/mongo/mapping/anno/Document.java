package com.echo.mongo.mapping.anno;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Document {

    /**
     * 映射mongodb的文档,若为空则以类名首字母小写为文档名
     * @return 文档名称
     */
    String collection() default "";

}
