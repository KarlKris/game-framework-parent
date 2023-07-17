package com.echo.ioc.context;

import com.echo.ioc.processor.*;
import com.echo.ioc.core.ConfigurableBeanFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * ApplicationContext 基类
 */
public abstract class AbstractApplicationContext implements ApplicationContext {

    /** 容器后置处理器 **/
    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new ArrayList<>();

    public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor processor) {
        this.beanFactoryPostProcessors.add(processor);
    }

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

        // 处理ApplicationContextAware
        beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
    }

    /**
     * 调用执行BeanFactoryPostProcessor 对容器进行处理
     * @param beanFactory 容器
     */
    protected void postBeanFactoryPostProcessors(final ConfigurableBeanFactory beanFactory) {

        invokeBeanFactoryPostProcessors(beanFactory, beanFactoryPostProcessors);

    }

    /**
     * 向容器注册BeanPostProcessor组件
     * @param beanFactory 容器
     */
    private void registerBeanPostProcessors(ConfigurableBeanFactory beanFactory) {
        Map<String, BeanPostProcessor> beans = beanFactory.getBeansByType(BeanPostProcessor.class);
        beans.values().forEach(beanFactory::addBeanPostProcessor);
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

        // Register bean processors that intercept bean creation.
        registerBeanPostProcessors(beanFactory);

        // 初始化非懒加载bean
        beanFactory.preInstantiateSingletons();
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

        Comparator<Ordered> comparator = Comparator.comparingInt(Ordered::getOrder);
        if (!beanFactoryPostProcessors.isEmpty()) {
            beanFactoryPostProcessors.sort(comparator);
            beanFactoryPostProcessors.forEach(beanFactoryPostProcessor
                    -> beanFactoryPostProcessor.postProcessBeanFactory(beanFactory));
        }

        List<BeanFactoryPostProcessor> processors = new ArrayList<>(beanFactory
                .getBeansByType(BeanFactoryPostProcessor.class).values());
        if (!processors.isEmpty()) {
            processors.sort(comparator);
            processors.forEach(beanFactoryPostProcessor
                    -> beanFactoryPostProcessor.postProcessBeanFactory(beanFactory));
        }
    }
}
