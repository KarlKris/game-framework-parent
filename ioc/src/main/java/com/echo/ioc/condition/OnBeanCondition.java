package com.echo.ioc.condition;

import cn.hutool.core.util.ArrayUtil;
import com.echo.ioc.anno.ConditionalOnBean;
import com.echo.ioc.anno.ConditionalOnMissingBean;
import com.echo.ioc.core.ConfigurableBeanFactory;
import com.echo.ioc.util.AnnotatedTypeMetadata;

import java.util.*;

/**
 * 基于BeanDefinition加载判断的Condition
 */
public class OnBeanCondition extends AbstractCondition implements ConfigurationCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage matchMessage = ConditionMessage.empty();
        ConfigurableBeanFactory beanFactory = context.getBeanFactory();
        ConditionalOnBean conditionalOnBean = metadata.getAnnotation(ConditionalOnBean.class);
        if (conditionalOnBean != null) {
            String[] classNames = conditionalOnBean.value();
            List<String> missing = filter(classNames, beanFactory, false);
            if (!missing.isEmpty()) {
                return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnBean.class)
                        .didNotFind("required bean", "required beans")
                        .items(ConditionMessage.Style.QUOTE, missing));
            }
            matchMessage = matchMessage.andCondition(ConditionalOnBean.class)
                    .found("required bean", "required beans")
                    .items(ConditionMessage.Style.QUOTE, filter(classNames, beanFactory, true));
        }
        ConditionalOnMissingBean conditionalOnMissingBean = metadata.getAnnotation(ConditionalOnMissingBean.class);
        if (conditionalOnMissingBean != null) {
            String[] classNames = conditionalOnMissingBean.value();
            List<String> missing = filter(classNames, beanFactory, true);
            if (!missing.isEmpty()) {
                return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnMissingBean.class)
                        .found("required bean", "required beans")
                        .items(ConditionMessage.Style.QUOTE, missing));
            }
            matchMessage = matchMessage.andCondition(ConditionalOnMissingBean.class)
                    .didNotFind("required bean", "required beans")
                    .items(ConditionMessage.Style.QUOTE, filter(classNames, beanFactory, false));
        }
        return ConditionOutcome.match(matchMessage);
    }

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    private static List<String> filter(String[] classNames, ConfigurableBeanFactory beanFactory, boolean contain) {
        if (ArrayUtil.isEmpty(classNames)) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>(classNames.length);
        for (String className : classNames) {
            List<String> beanNames = null;
            try {
                Class<?> clz = resolve(className, null);
                beanNames = beanFactory.getBeanNamesByType(clz);
            } catch (ClassNotFoundException e) {
                // ignore
            } finally {
                if ((beanNames == null || beanNames.isEmpty()) && !contain) {
                    list.add(className);
                } else if (contain) {
                    list.add(className);
                }
            }
        }
        return list;
    }
}
