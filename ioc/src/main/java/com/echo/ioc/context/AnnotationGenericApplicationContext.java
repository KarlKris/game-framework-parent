package com.echo.ioc.context;

import com.echo.ioc.core.ConfigurableBeanFactory;
import com.echo.ioc.core.DefaultBeanFactory;
import com.echo.ioc.loader.BeanDefinitionLoader;
import com.echo.ioc.loader.ClassBeanDefinitionScanner;
import com.echo.ioc.processor.ConfigurationClassPostProcessor;
import com.echo.ioc.processor.ConfigurationPropertiesBeanFactoryPostProcessor;

/**
 * 基于注解注册单例的ApplicationContext
 */
public class AnnotationGenericApplicationContext extends AbstractApplicationContext {

    /** bean 容器 **/
    private final DefaultBeanFactory beanFactory;
    /** beanDefinitionLoader **/
    private final BeanDefinitionLoader beanDefinitionLoader;

    public AnnotationGenericApplicationContext(String... basePackages) {
        this.beanFactory = new DefaultBeanFactory();
        this.beanDefinitionLoader = new ClassBeanDefinitionScanner(beanFactory, basePackages);

        addBeanFactoryPostProcessor(new ConfigurationClassPostProcessor());
        addBeanFactoryPostProcessor(new ConfigurationPropertiesBeanFactoryPostProcessor());
    }

    @Override
    protected void closeBeanFactory() {
        // nothing to do
    }

    @Override
    protected ConfigurableBeanFactory createBeanFactory() {
        return beanFactory;
    }

    @Override
    protected void loadBeanDefinitions() {
        beanDefinitionLoader.loadBeanDefinitions();
    }

    @Override
    public ConfigurableBeanFactory getBeanFactory() {
        return beanFactory;
    }
}
