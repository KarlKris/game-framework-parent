package com.echo.ioc.processor;

import cn.hutool.core.annotation.AnnotationUtil;
import com.echo.common.util.StringUtils;
import com.echo.ioc.anno.Bean;
import com.echo.ioc.condition.ConditionEvaluator;
import com.echo.ioc.condition.ConfigurationClassParser;
import com.echo.ioc.condition.ConfigurationCondition.ConfigurationPhase;
import com.echo.ioc.core.ConfigurableBeanFactory;
import com.echo.ioc.loader.BeanDefinition;
import com.echo.ioc.loader.BeanDefinitionRegistry;
import com.echo.ioc.loader.MethodBeanDefinition;
import com.echo.ioc.util.BeanFactoryUtil;
import com.echo.ioc.util.StandardMethodMetadata;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link com.echo.ioc.anno.Configuration} 注册Bean
 */
public class ConfigurationClassPostProcessor implements BeanFactoryPostProcessor {

    public static final String BEAN_NAME = "configurationClassPostProcessor";

    @Override
    public void postProcessBeanFactory(ConfigurableBeanFactory beanFactory) {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

        List<BeanDefinition> configurationBeanDefinitions = new ArrayList<>();
        for (String beanDefinitionName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanDefinitionName);
            if (!beanDefinition.isConfigurationClass()) {
                continue;
            }
            configurationBeanDefinitions.add(beanDefinition);
        }

        if (configurationBeanDefinitions.isEmpty()) {
            return;
        }

        ConfigurationClassParser parser = new ConfigurationClassParser();
        ConditionEvaluator conditionEvaluator = new ConditionEvaluator(registry);
        // 解析
        parser.parse(configurationBeanDefinitions);
        // Condition条件再判断
        for (BeanDefinition definition : configurationBeanDefinitions) {
            if (conditionEvaluator.shouldSkip(definition.getMetadata(), ConfigurationPhase.REGISTER_BEAN)) {
                if (registry.containsBeanDefinition(definition.getBeanName())) {
                    registry.removeBeanDefinition(definition.getBeanName());
                }
                continue;
            }
            for (Method beanMethod : definition.getBeanMethods()) {
                if (conditionEvaluator.shouldSkip(new StandardMethodMetadata(beanMethod), ConfigurationPhase.REGISTER_BEAN)) {
                    continue;
                }
                Bean bean = AnnotationUtil.getAnnotation(beanMethod, Bean.class);
                Class<?> returnType = beanMethod.getReturnType();
                String innerBeanName = bean.value();
                boolean lazyInit = bean.isLazyInit();
                if (!StringUtils.hasLength(innerBeanName)) {
                    innerBeanName = BeanFactoryUtil.generateBeanName(returnType);
                }
                registry.registerBeanDefinition(new MethodBeanDefinition(innerBeanName, returnType
                        , definition.getBeanName(), beanMethod, lazyInit));
            }
        }

    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
