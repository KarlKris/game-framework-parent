package com.li.ioc.context;

import com.li.ioc.core.ConfigurableBeanFactory;
import com.li.ioc.core.DefaultBeanFactory;
import com.li.ioc.loader.BeanDefinitionLoader;
import com.li.ioc.loader.ClassBeanDefinitionScanner;

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
