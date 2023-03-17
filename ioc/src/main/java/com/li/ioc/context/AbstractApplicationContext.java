package com.li.ioc.context;

import com.li.ioc.core.BeanFactory;

public abstract class AbstractApplicationContext implements ApplicationContext {

    /** 真正的容器 **/
    private BeanFactory beanFactory;

    /** 留给子类去创建容器 **/
    protected abstract BeanFactory createBeanFactory();

    /** 刷新容器 **/
    private void refreshBeanFactory() {
        if (hasBeanFactory()) {
            destroyBeans();
            closeBeanFactory();
        }
        this.beanFactory = createBeanFactory();
    }


    private void destroyBeans() {
        getBeanFactory().destroy();
    }

    private void closeBeanFactory() {
        BeanFactory beanFactory = getBeanFactory();
        if (beanFactory != null) {
            this.beanFactory = null;
        }
    }

    private boolean hasBeanFactory() {
        return getBeanFactory() != null;
    }

    /**
     * 容器预备处理
     *
     * @param beanFactory beanFactory
     */
    protected void prepareBeanFactory(BeanFactory beanFactory) {
        // todo
    }

    // ---------------------------------------


    @Override
    public void refresh() {
        refreshBeanFactory();

        BeanFactory beanFactory = getBeanFactory();

        prepareBeanFactory(beanFactory);


    }

    @Override
    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    @Override
    public <T> T getBean(Class<T> clz) {
        return getBeanFactory().getBean(clz);
    }
}
