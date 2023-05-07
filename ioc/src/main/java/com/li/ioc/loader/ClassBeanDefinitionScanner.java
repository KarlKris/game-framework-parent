package com.li.ioc.loader;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.util.ArrayUtil;
import com.li.common.util.ReflectionUtil;
import com.li.common.util.StringUtil;
import com.li.ioc.anno.Bean;
import com.li.ioc.anno.Component;
import com.li.ioc.anno.Configuration;
import com.li.ioc.util.BeanFactoryUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 扫描指定包下的包含指定注解的单例Bean的 BeanDefinition对象
 * 读取@Component和@Configuration下的所有@Bean
 */
public class ClassBeanDefinitionScanner implements BeanDefinitionLoader {

    /** 扫描的包 **/
    private final String[] basePackages;
    /** 加载器 **/
    private final BeanDefinitionRegistry registry;

    public ClassBeanDefinitionScanner(BeanDefinitionRegistry registry, String... basePackages) {
        this.registry = registry;
        this.basePackages = basePackages;
    }

    private List<BeanDefinition> scan() {
        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        if (ArrayUtil.isEmpty(basePackages)) {
            scan0("", beanDefinitions);
        } else {
            for (String basePackage : basePackages) {
                scan0(basePackage, beanDefinitions);
            }
        }
        return beanDefinitions;
    }

    private void scan0(String basePackage, List<BeanDefinition> beanDefinitions) {
        for (Class<?> clz : new ClassScanner(basePackage).scan()) {
            resolveClz(clz, beanDefinitions);
        }
    }

    private void resolveClz(Class<?> clz, List<BeanDefinition> beanDefinitions) {
        if (clz.isAnnotation() || clz.isAnonymousClass() || clz.isInterface()) {
            return;
        }
        // 处理@Component
        Component component = AnnotationUtil.getAnnotation(clz, Component.class);
        if (component == null) {
            return;
        }
        String beanName = component.value();
        if (!StringUtil.hasLength(beanName)) {
            beanName = BeanFactoryUtil.generateBeanName(clz);
        }
        beanDefinitions.add(new BeanDefinition(beanName, clz));
        // 处理@Configuration
        Configuration configuration = AnnotationUtil.getAnnotation(clz, Configuration.class);
        if (configuration == null) {
            return;
        }
        for (Method method : ReflectionUtil.getMethods(clz
                , method -> AnnotationUtil.hasAnnotation(method, Bean.class))) {
            Bean bean = AnnotationUtil.getAnnotation(method, Bean.class);
            Class<?> returnType = method.getReturnType();
            String innerBeanName = bean.value();
            if (!StringUtil.hasLength(innerBeanName)) {
                innerBeanName = BeanFactoryUtil.generateBeanName(returnType);
            }
            beanDefinitions.add(new BeanDefinition(innerBeanName, returnType, beanName, method));
        }
    }

    @Override
    public int loadBeanDefinitions() {
        List<BeanDefinition> beanDefinitions = scan();
        registry.registerBeanDefinitions(beanDefinitions);
        return beanDefinitions.size();
    }
}
