package com.li.ioc.core;

import com.li.common.util.StringUtil;
import com.li.ioc.anno.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author li-yuanwen
 * @date 2023/03/16
 */
public abstract class AbstractBeanFactory implements BeanFactory {

    /** 单例缓存 key:beanName-->value:instance **/
    private final Map<String, Object> singletonObjects = new HashMap<>(64);
    /** 二级缓存（用于未完全注入field的object） **/
    private final Map<String, Object> earlySingletonObjects = new HashMap<>();


    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clz) {
        // todo clz转beanName
        String beanName = generateBeanName(clz);
        Object singleton = getSingleton(beanName);
        if (singleton == null) {
            singleton = getSingleton(beanName, () -> createBean(clz));
        }
        return (T) singleton;
    }

    @Override
    public void destroy() {
        singletonObjects.clear();
    }

    // ------------------------------------------------------------------------------------


    // ------------------------------------------------------------------------------------

    /**
     * 根据 beanName 获取单例bean对象
     * @param beanName beanName
     * @return bean
     */
    private Object getSingleton(String beanName) {
        Object singleObject = singletonObjects.get(beanName);
        if (singleObject == null) {
            singleObject = earlySingletonObjects.get(beanName);
        }
        return singleObject;
    }

    private Object getSingleton(String beanName, ObjectFactory<?> objectFactory) {
        Object singletonObject = singletonObjects.get(beanName);
        if (singletonObject == null) {
            synchronized (singletonObjects) {
                singletonObject = singletonObjects.get(beanName);
                if (singletonObject == null) {
                    singletonObject = objectFactory.getObject();
                    singletonObjects.put(beanName, singletonObject);
                }
            }
        }
        return singletonObject;
    }

    private Object createBean(Class<?> clz) {
        return null;
    }


    /**
     * 根据class生成beanName
     * @param clz clz
     * @return beanName
     */
    private String generateBeanName(Class<?> clz) {
        Component component = clz.getAnnotation(Component.class);
        if (component != null && StringUtil.hasLength(component.name())) {
            return component.name();
        }
        return StringUtil.lowerFirst(clz.getSimpleName());
    }

    
}
