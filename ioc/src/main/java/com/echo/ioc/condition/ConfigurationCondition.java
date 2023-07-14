package com.echo.ioc.condition;

/**
 * {@code @Configuration}注解所使用的Condition
 */
public interface ConfigurationCondition extends Condition {


    /** 处理的阶段 **/
    ConfigurationPhase getConfigurationPhase();


    /** 阶段 **/
    enum ConfigurationPhase {

        /** {@code @Configuration}修饰的类解析阶段 {@link Condition}   **/
        PARSE_CONFIGURATION,

        /** {@code @Configuration}修饰的类内注册Bean注册进BeanFactory后的再判断阶段 {@link Condition}   **/
        REGISTER_BEAN,

    }

}
