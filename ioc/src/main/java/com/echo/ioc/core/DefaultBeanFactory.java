package com.echo.ioc.core;

import com.echo.common.util.StringUtils;
import com.echo.ioc.exception.NoSuchBeanDefinitionException;
import com.echo.ioc.exception.NoSuchBeanException;
import com.echo.ioc.exception.NoUniqueBeanDefinitionException;
import com.echo.ioc.loader.BeanDefinition;
import com.echo.ioc.loader.BeanDefinitionRegistry;

import java.util.*;

/**
 * BeanFactory 默认实现
 */
public class DefaultBeanFactory extends AbstractBeanFactory implements BeanDefinitionRegistry {

    /** beanDefinitions 容器,当bean被创建时移除 **/
    private final Map<String, BeanDefinition> beanDefinitions = new HashMap<>();


    // -------------------------------------------------------------------------------

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        BeanDefinition beanDefinition = beanDefinitions.get(beanName);
        if (beanDefinition == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }
        return beanDefinition;
    }

    /**
     * 向容器注册BeanDefinition集
     * @param beanDefinitions 单例Bean定义信息集
     */
    public void registerBeanDefinitions(Collection<BeanDefinition> beanDefinitions) {
        beanDefinitions.forEach(this::registerBeanDefinition);
    }

    /**
     * 向容器注册BeanDefinition
     * @param beanDefinition 单例Bean定义信息
     */
    public void registerBeanDefinition(BeanDefinition beanDefinition) {
        BeanDefinition old = beanDefinitions.putIfAbsent(beanDefinition.getBeanName(), beanDefinition);
        if (old != null) {
            throw new IllegalStateException("multiple Same BeanName: " + beanDefinition.getBeanName());
        }
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return beanDefinitions.containsKey(beanName);
    }

    @Override
    public void removeBeanDefinition(String beanName) {
        beanDefinitions.remove(beanName);
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return StringUtils.toStringArray(beanDefinitions.keySet());
    }

    // -------------------------------------------------------------------------------


    @Override
    protected <T> T getBean(Class<T> requiredType, boolean required) {
        List<String> beanNames = getBeanNamesByType(requiredType);
        int size = beanNames.size();
        if (size == 0 && required) {
            throw new NoSuchBeanException("no such bean class: " + requiredType.getName());
        }
        if (size != 1) {
            throw new NoUniqueBeanDefinitionException(requiredType, beanNames);
        }
        return getBean(beanNames.get(0), requiredType);
    }

    @Override
    public List<String> getBeanNamesByType(Class<?> requiredType) {
        List<String> list = new LinkedList<>();
        // beanDefinition未实例的单例
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
            BeanDefinition definition = entry.getValue();
            if (!requiredType.isAssignableFrom(definition.getBeanClz())) {
                continue;
            }
            list.add(entry.getKey());
        }
        // 已实例的单例
        for (Map.Entry<String, Object> entry : getSingletonObjects().entrySet()) {
            Object singletonObject = entry.getValue();
            if (!requiredType.isAssignableFrom(singletonObject.getClass())) {
                continue;
            }
            list.add(entry.getKey());
        }
        return list;
    }

    @Override
    public <T> Map<String, T> getBeansByType(Class<T> requiredType) {
        Collection<String> beanNames = getBeanNamesByType(requiredType);
        if (beanNames.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, T> map = new HashMap<>(beanNames.size());
        for (String beanName : beanNames) {
            T bean = getBean(beanName, requiredType);
            map.put(beanName, bean);
        }
        return map;
    }

    @Override
    protected void afterAddSingleton(String beanName) {
        // 添加单例后就删除BeanDefinition
        removeBeanDefinition(beanName);
    }

    // -------------------------------------------------------------------------------




}
