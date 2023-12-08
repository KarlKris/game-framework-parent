package com.echo.ioc.core;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import com.echo.common.util.ClassUtils;
import com.echo.common.util.ObjectUtils;
import com.echo.common.util.ReflectionUtils;
import com.echo.common.util.StringUtils;
import com.echo.ioc.anno.Autowired;
import com.echo.ioc.anno.Qualifier;
import com.echo.ioc.anno.Value;
import com.echo.ioc.exception.BeanCreateException;
import com.echo.ioc.exception.BeanCurrentlyInCreationException;
import com.echo.ioc.exception.BeanNotOfRequiredTypeException;
import com.echo.ioc.exception.NoSuchBeanDefinitionException;
import com.echo.ioc.loader.BeanDefinition;
import com.echo.ioc.loader.MethodBeanDefinition;
import com.echo.ioc.loader.PropertiesBeanDefinition;
import com.echo.ioc.processor.BeanPostProcessor;
import com.echo.ioc.processor.InstantiationAwareBeanPostProcessor;
import com.echo.ioc.prop.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author li-yuanwen
 * @date 2023/03/16
 */
@Slf4j
public abstract class AbstractBeanFactory implements ConfigurableBeanFactory {

    /** 单例缓存 key:beanName-->value:instance **/
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(64);
    /** 二级缓存（用于未完全注入field的object） **/
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();

    /** 正在创建beanName **/
    private final Set<String> singletonsCurrentlyInCreation = new ConcurrentHashSet<>(64);

    /** bean实例化后置处理器 **/
    private final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();

    /** 属性配置 **/
    private final MutablePropertySources propertySources = new MutablePropertySources();

    /** 属性解析 **/
    private final PropertyResolver propertyResolver = new PropertySourcesPropertyResolver(propertySources);

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        if (beanPostProcessor == null) {
            return;
        }
        // 先移除旧的
        beanPostProcessors.remove(beanPostProcessor);
        // 再添加新的
        beanPostProcessors.add(beanPostProcessor);
    }

    public void addBeanPostProcessors(Collection<? extends BeanPostProcessor> beanPostProcessors) {
        if (beanPostProcessors == null || beanPostProcessors.isEmpty()) {
            return;
        }
        // 先移除旧的
        this.beanPostProcessors.removeAll(beanPostProcessors);
        // 再添加新的
        this.beanPostProcessors.addAll(beanPostProcessors);
    }

    @Override
    public Object getBean(String beanName) {
        Object singleton = getSingleton(beanName);
        if (singleton == null) {
            singleton = getSingleton(beanName
                    , () -> createBean(beanName));
        }
        return singleton;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanName, Class<T> requiredType) {
        Object singleton = getBean(beanName);
        if (!requiredType.isAssignableFrom(singleton.getClass())) {
            throw new BeanNotOfRequiredTypeException(beanName, requiredType, singleton.getClass());
        }
        return (T) singleton;
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return getBean(requiredType, true);
    }

    @Override
    public void destroy() {
        synchronized (singletonObjects) {
            for (Object bean : singletonObjects.values()) {
                invokePreDestroyMethod(bean);
            }
        }
        singletonObjects.clear();
    }

    @Override
    public void addPropertySources(PropertySources propertySources) {
        propertySources.stream().forEach(this::addPropertySource);
    }

    @Override
    public void addPropertySource(PropertySource<?> propertySource) {
        this.propertySources.addLast(propertySource);
    }

    @Override
    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

    // ------------------------------------------------------------------------------------


    /**
     * 根据beanName获取BeanDefinition
     * @param beanName beanName
     * @return BeanDefinition
     */
    protected abstract BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    /**
     * 根据clz获取bean单例对象 or 在允许为null的情况下返回null
     * @param requiredType clz
     * @param required bean是否必须存在
     * @return bean or null
     * @param <T> bean Class
     */
    protected abstract <T> T getBean(Class<T> requiredType, boolean required);

    // ------------------------------------------------------------------------------------

    /**
     * 根据 beanName 获取单例bean对象
     * @param beanName beanName
     * @return bean
     */
    @Override
    public Object getSingleton(String beanName) {
        Object singleObject = singletonObjects.get(beanName);
        if (singleObject == null) {
            synchronized (singletonObjects) {
                singleObject = singletonObjects.get(beanName);
                if (singleObject == null) {
                    singleObject = earlySingletonObjects.get(beanName);
                }
            }
        }
        return singleObject;
    }

    @Override
    public void addSingleton(String beanName, Object singleton) {
        synchronized (singletonObjects) {
            singletonObjects.put(beanName, singleton);
            earlySingletonObjects.remove(beanName);
        }
    }

    /** 添加单例后处理 **/
    protected void afterAddSingleton(String beanName) {
        // nothing to do 留给子类处理
    }

    // ------------------------------------------------------------------------------------

    /** 单例对象 **/
    protected Map<String, Object> getSingletonObjects() {
        return Collections.unmodifiableMap(singletonObjects);
    }

    // ------------------------------------------------------------------------------------

    private Object getSingleton(String beanName, ObjectFactory<?> objectFactory) {
        Object singletonObject = singletonObjects.get(beanName);
        if (singletonObject == null) {
            synchronized (singletonObjects) {
                singletonObject = singletonObjects.get(beanName);
                if (singletonObject == null) {
                    beforeSingletonCreation(beanName);
                    try {
                        singletonObject = objectFactory.getObject();
                    } finally {
                        afterSingletonCreation(beanName);
                    }
                    if (singletonObject != null) {
                        addSingleton(beanName, singletonObject);
                        afterAddSingleton(beanName);
                    }
                }
            }
        }
        return singletonObject;
    }

    private boolean isSingletonCurrentlyInCreation(String beanName) {
        return singletonsCurrentlyInCreation.contains(beanName);
    }

    private void beforeSingletonCreation(String beanName) {
        if (!singletonsCurrentlyInCreation.add(beanName)) {
            throw new BeanCurrentlyInCreationException( beanName);
        }
    }

    private void afterSingletonCreation(String beanName) {
        if (!singletonsCurrentlyInCreation.remove(beanName)) {
            throw new IllegalStateException("Singleton '" + beanName + "' isn't currently in creation");
        }
    }

    private Object createBean(String beanName) {
        // 查找BeanDefinition
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        Object instance = resolveBeforeInstantiation(beanName, beanDefinition);
        if (instance != null) {
            return instance;
        }

        instance = newInstance(beanDefinition);
        // 放入earlySingletonObjects中
        if (isSingletonCurrentlyInCreation(beanName)) {
            addEarlySingletonObject(beanName, instance);
        }
        try {
            populateBean(beanDefinition, instance);
            return initializeBean(beanName, instance);
        } catch (IllegalAccessException e) {
            throw new BeanCreateException(beanName, "createBean error ", e);
        }
    }

    private void addEarlySingletonObject(String beanName, Object earlySingletonObject) {
        synchronized (singletonObjects) {
            if (!singletonObjects.containsKey(beanName)) {
                earlySingletonObjects.put(beanName, earlySingletonObject);
            }
        }
    }

    private Object newInstance(BeanDefinition beanDefinition) {
        if (beanDefinition instanceof MethodBeanDefinition) {
            return newInstanceByFactoryMethod((MethodBeanDefinition) beanDefinition);
        }
        return newInstanceByConstructor(beanDefinition);
    }

    private Object newInstanceByFactoryMethod(MethodBeanDefinition beanDefinition) {
        Method factoryMethod = beanDefinition.getFactoryMethod();
        Object bean = getBean(beanDefinition.getFactoryBeanName());
        Object[] args = new Object[factoryMethod.getParameterCount()];
        int index = 0;
        for (Class<?> parameterType : factoryMethod.getParameterTypes()) {
            args[index++] = getBean(parameterType);
        }
        try {
            return factoryMethod.invoke(bean, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    private Object newInstanceByConstructor(BeanDefinition beanDefinition) {
        Constructor<?> constructor = findConstructor(beanDefinition.getBeanClz());
        if (constructor == null) {
            throw new BeanCreateException(beanDefinition.getBeanName(), "not suitable Constructor");
        }
        Object[] constArgs = new Object[constructor.getParameterCount()];
        int index = 0;
        for (Class<?> parameterType : constructor.getParameterTypes()) {
            constArgs[index++] = getBean(parameterType);
        }
        try {
            return constructor.newInstance(constArgs);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new BeanCreateException(beanDefinition.getBeanName(), " newInstance error", e);
        }

    }

    private Constructor<?> findConstructor(Class<?> clz) {
        Constructor<?> constToUse = null;
        Constructor<?>[] constructors = clz.getConstructors();
        if (constructors.length == 1) {
            constToUse = constructors[0];
        } else {
            for (Constructor<?> constructor : constructors) {
                // 有参构造函数的参数必须使用@Autowired注解注入
                boolean match = true;
                Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
                for (Annotation[] annotations : parameterAnnotations) {
                    if (!ArrayUtil.contains(annotations, Autowired.class)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    constToUse = constructor;
                    break;
                }
            }
        }
        return constToUse;
    }


    private void populateBean(BeanDefinition beanDefinition, Object instance) throws IllegalAccessException {
        // bean实例化后置
        invokePostProcessAfterInstantiation(beanDefinition.getBeanName(), instance);

        // 处理PropertiesBeanDefinition
        if (beanDefinition instanceof PropertiesBeanDefinition) {
            populatePropertiesBean((PropertiesBeanDefinition) beanDefinition, instance);
            return;
        }

        // 注入field
        for (Field field : beanDefinition.getFields()) {
            Resource resource = AnnotationUtil.getAnnotation(field, Resource.class);
            if (resource != null) {
                autowiredByName(instance, field, resource.name());
                continue;
            }
            Autowired autowired = AnnotationUtil.getAnnotation(field, Autowired.class);
            if (autowired != null) {
                autowiredByType(instance, field, autowired.required());
                continue;
            }
            Value value = AnnotationUtil.getAnnotation(field, Value.class);
            if (value != null) {
                resolveValueExpression(instance, field, value);
            }
        }
    }

    private void populatePropertiesBean(PropertiesBeanDefinition beanDefinition, Object instance) throws IllegalAccessException {
        String prefixName = beanDefinition.getPropertyPrefixName();
        // 注入field
        for (Field field : beanDefinition.getFields()) {
            populatePropertiesField(instance, field, prefixName);
        }
    }

    private void populatePropertiesField(Object instance, Field field, String prefixName) throws IllegalAccessException {
        Class<?> type = field.getType();
        Object value = null;
        if (ClassUtils.isSimpleTypeOrArray(type)) {
            String name = field.getName();
            String propertyName = prefixName + "." + name;
            value = propertyResolver.getProperty(propertyName, field.getType());
        } else {
            prefixName = prefixName + "." + field.getName();
            value = ObjectUtils.newInstance(type);
            populatePropertiesFieldInstance(value, prefixName);
        }
        if (value == null) {
            return;
        }
        ReflectionUtils.makeAccessible(field);
        field.set(instance, value);
    }

    private void populatePropertiesFieldInstance(Object instance, String prefixName) throws IllegalAccessException {
        for (Field field : ReflectionUtils.getFields(instance.getClass())) {
            populatePropertiesField(instance, field, prefixName);
        }
    }

    private void autowiredByName(Object instance, Field field, String beanName) throws IllegalAccessException {
        String name = beanName;
        if (!StringUtils.hasLength(beanName)) {
            name = field.getName();
        }

        Object bean = getBean(name, field.getType());
        ReflectionUtils.makeAccessible(field);
        field.set(instance, bean);
    }

    private void autowiredByType(Object instance, Field field, boolean required) throws IllegalAccessException {
        Object bean;
        Qualifier qualifier = AnnotationUtil.getAnnotation(field, Qualifier.class);
        if (qualifier != null) {
            String beanName = qualifier.value();
            if (!StringUtils.hasLength(beanName)) {
                throw new IllegalAccessException("not qualifier beanName: "
                        + instance.getClass().getSimpleName()
                        + "." + field.getName());
            }
            bean = getBean(beanName);
            if (bean == null && required) {
                throw new IllegalAccessException("no beanName:" + beanName);
            }
        } else {
            bean = getBean(field.getType(), required);
        }
        ReflectionUtils.makeAccessible(field);
        field.set(instance, bean);
    }

    private void resolveValueExpression(Object instance, Field field, Value annotation) throws IllegalAccessException {
        String expression = annotation.value();
        if (!StringUtils.hasLength(expression)) {
            throw new IllegalStateException("@Value.value is Blank: "
                    + instance.getClass().getSimpleName() + "." + field.getName());
        }
        String[] array = expression.split(DEFAULT_VALUE_SEPARATOR);
        String propertyName = array[0];
        Object value = propertyResolver.getProperty(propertyName, field.getType());
        if (value == null) {
            if (array.length > 1) {
                String defaultValue = array[1];
                value = Convert.convert(field.getType(), defaultValue);
            } else if (annotation.required()) {
                throw new IllegalStateException("@Value.value not property: "
                        + instance.getClass().getSimpleName() + "." + field.getName());
            }
        }
        ReflectionUtils.makeAccessible(field);
        field.set(instance, value);
    }

    private Object initializeBean(String beanName, Object bean) {
        bean = applyPostProcessBeforeInitialization(bean, beanName);
        try {
            invokeInitMethod(bean);
        } catch (Throwable e) {
            throw new BeanCreateException(beanName, "Invocation of init method failed", e);
        }
        bean = applyPostProcessAfterInitialization(bean, beanName);
        return bean;
    }

    private void invokeInitMethod(Object instance) throws InvocationTargetException, IllegalAccessException {
        // @PostConstruct修饰的方法调用
        for (Method method : ReflectionUtils.getMethods(instance.getClass()
                , method -> AnnotationUtil.hasAnnotation(method, PostConstruct.class))) {
            ReflectionUtils.makeAccessible(method);
            method.invoke(instance);
            break;
        }
    }

    private void invokePreDestroyMethod(Object instance) {
        // @PostConstruct修饰的方法调用
        for (Method method : ReflectionUtils.getMethods(instance.getClass()
                , method -> AnnotationUtil.hasAnnotation(method, PreDestroy.class))) {
            try {
                ReflectionUtils.makeAccessible(method);
                method.invoke(instance);
                break;
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("bean invoke preDestroy method error: ", e);
            }
        }
    }

    public Object resolveBeforeInstantiation(String beanName, BeanDefinition beanDefinition) {
        Object bean = null;
        for (BeanPostProcessor processor : beanPostProcessors) {
            if (!(processor instanceof InstantiationAwareBeanPostProcessor)) {
                continue;
            }
            InstantiationAwareBeanPostProcessor iProcessor = (InstantiationAwareBeanPostProcessor) processor;
            bean = iProcessor.postProcessBeforeInstantiation(beanDefinition.getBeanClz(), beanName);
            if (bean != null) {
                bean = applyPostProcessAfterInitialization(bean, beanName);
            }
        }
        return bean;
    }

    private Object applyPostProcessBeforeInitialization(Object bean, String beanName) {
        Object result = bean;
        for (BeanPostProcessor processor : beanPostProcessors) {
            Object current = processor.postProcessBeforeInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    private Object applyPostProcessAfterInitialization(Object bean, String beanName) {
        Object result = bean;
        for (BeanPostProcessor processor : beanPostProcessors) {
            Object current = processor.postProcessAfterInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    private void invokePostProcessAfterInstantiation(String beanName, Object bean) {
        for (BeanPostProcessor processor : beanPostProcessors) {
            if (!(processor instanceof InstantiationAwareBeanPostProcessor)) {
                continue;
            }
            InstantiationAwareBeanPostProcessor iProcessor = (InstantiationAwareBeanPostProcessor) processor;
            if (!iProcessor.postProcessAfterInstantiation(bean, beanName)) {
                break;
            }
        }
    }
    
}
