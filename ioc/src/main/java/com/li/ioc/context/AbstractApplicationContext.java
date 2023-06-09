package com.li.ioc.context;

import com.li.ioc.core.ConfigurableBeanFactory;
import com.li.ioc.processor.BeanFactoryPostProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ApplicationContext 基类
 */
public abstract class AbstractApplicationContext implements ApplicationContext {

    /** 容器后置处理器 **/
    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new ArrayList<>();


    /** 刷新容器 **/
    private void refreshBeanFactory() {
        if (hasBeanFactory()) {
            destroyBeans();
            closeBeanFactory();
        }
        createBeanFactory();
        loadBeanDefinitions();
    }


    private boolean hasBeanFactory() {
        return getBeanFactory() != null;
    }

    /**
     * 容器预备处理
     *
     * @param beanFactory beanFactory
     */
    protected void prepareBeanFactory(ConfigurableBeanFactory beanFactory) {
        // 注册自身
        beanFactory.addSingleton("applicationContext", this);

        // todo
    }

    /**
     * 调用执行BeanFactoryPostProcessor 对容器进行处理
     * @param beanFactory 容器
     */
    protected void postBeanFactoryPostProcessors(final ConfigurableBeanFactory beanFactory) {

        invokeBeanFactoryPostProcessors(beanFactory, beanFactoryPostProcessors);

    }

    private void destroyBeans() {
        getBeanFactory().destroy();
    }


    /** 关闭容器 **/
    protected abstract void closeBeanFactory();

    /** 留给子类去创建容器 **/
    protected abstract ConfigurableBeanFactory createBeanFactory();

    /** 读取BeanDefinitions,并注册到beanFactory容器里 **/
    protected abstract void loadBeanDefinitions();

    // -------------------------------------------------------------------------------------


    @Override
    public void close() {
        destroyBeans();
    }

    @Override
    public void refresh() {
        refreshBeanFactory();

        ConfigurableBeanFactory beanFactory = getBeanFactory();
        prepareBeanFactory(beanFactory);

        // 执行BeanFactoryPostProcessors,容器后置处理
        postBeanFactoryPostProcessors(beanFactory);
    }

    @Override
    public Object getBean(String beanName) {
        return getBeanFactory().getBean(beanName);
    }

    @Override
    public <T> T getBean(Class<T> clz) {
        return getBeanFactory().getBean(clz);
    }

    @Override
    public <T> T getBean(String beanName, Class<T> requiredType) {
        return getBeanFactory().getBean(beanName, requiredType);
    }

    @Override
    public List<String> getBeanNamesByType(Class<?> requiredType) {
        return getBeanFactory().getBeanNamesByType(requiredType);
    }

    @Override
    public <T> Map<String, T> getBeansByType(Class<T> requiredType) {
        return getBeanFactory().getBeansByType(requiredType);
    }

    public static void invokeBeanFactoryPostProcessors(final ConfigurableBeanFactory beanFactory
            , final List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

        if (!beanFactoryPostProcessors.isEmpty()) {
            beanFactoryPostProcessors.forEach(beanFactoryPostProcessor
                    -> beanFactoryPostProcessor.postProcessBeanFactory(beanFactory));
        }

        // BeanFactoryPostProcessor bean
        beanFactory.getBeansByType(BeanFactoryPostProcessor.class)
                .values().forEach(beanFactoryPostProcessor
                        -> beanFactoryPostProcessor.postProcessBeanFactory(beanFactory));
    }
}
