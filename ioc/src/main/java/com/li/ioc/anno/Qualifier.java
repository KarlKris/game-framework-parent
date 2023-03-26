package com.li.ioc.anno;

import java.lang.annotation.*;

/**
 * beanName指定,配合@Autowired使用
 * @author li-yuanwen
 * @date 2023/3/26
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Qualifier {
}
