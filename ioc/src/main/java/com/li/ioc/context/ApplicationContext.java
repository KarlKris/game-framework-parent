package com.li.ioc.context;

import com.li.ioc.core.BeanFactory;
import com.li.ioc.core.ConfigurableBeanFactory;

/**
 * 容器上下文
 */
public interface ApplicationContext extends BeanFactory {


    /** 获取真正的容器 **/
    ConfigurableBeanFactory getBeanFactory();


    /** 刷新容器,开始注册实例 **/
    void refresh();

}
