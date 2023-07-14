package com.echo.ioc.anno;

import com.echo.ioc.condition.OnBeanCondition;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Conditional(OnBeanCondition.class)
public @interface ConditionalOnBean {

    /**
     * The class type names of beans that should be checked. The condition matches when
     * beans of all classes specified are contained in the {@link com.echo.ioc.core.BeanFactory}.
     * @return the class type names of beans to check
     */
    String[] value() default {};

}
