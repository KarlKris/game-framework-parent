package com.echo.ioc.loader;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.util.ArrayUtil;
import com.echo.common.util.StringUtils;
import com.echo.ioc.anno.Component;
import com.echo.ioc.condition.ConditionEvaluator;
import com.echo.ioc.util.BeanFactoryUtil;
import com.echo.ioc.util.StandardClassMetadata;
import com.echo.ioc.condition.ConfigurationCondition.ConfigurationPhase;

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
        ConditionEvaluator conditionEvaluator = new ConditionEvaluator(registry);
        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        if (ArrayUtil.isEmpty(basePackages)) {
            scan0(conditionEvaluator, "", beanDefinitions);
        } else {
            for (String basePackage : basePackages) {
                scan0(conditionEvaluator, basePackage, beanDefinitions);
            }
        }
        return beanDefinitions;
    }

    private void scan0(ConditionEvaluator conditionEvaluator, String basePackage, List<BeanDefinition> beanDefinitions) {
        for (Class<?> clz : new ClassScanner(basePackage).scan()) {
            resolveClz(conditionEvaluator, clz, beanDefinitions);
        }
    }

    private void resolveClz(ConditionEvaluator conditionEvaluator, Class<?> clz, List<BeanDefinition> beanDefinitions) {
        if (clz.isAnnotation() || clz.isAnonymousClass() || clz.isInterface()) {
            return;
        }
        // 处理@Component
        Component component = AnnotationUtil.getAnnotation(clz, Component.class);
        if (component == null) {
            return;
        }
        if (conditionEvaluator.shouldSkip(new StandardClassMetadata(clz), ConfigurationPhase.PARSE_CONFIGURATION)) {
            return;
        }

        String beanName = component.value();
        if (!StringUtils.hasLength(beanName)) {
            beanName = BeanFactoryUtil.generateBeanName(clz);
        }
        beanDefinitions.add(new BeanDefinition(beanName, clz));
    }

    @Override
    public int loadBeanDefinitions() {
        List<BeanDefinition> beanDefinitions = scan();
        registry.registerBeanDefinitions(beanDefinitions);
        return beanDefinitions.size();
    }
}
