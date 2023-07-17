package com.echo.ioc.condition;

import cn.hutool.core.util.ArrayUtil;
import com.echo.common.util.StringUtils;
import com.echo.ioc.anno.ConditionalOnProperty;
import com.echo.ioc.core.ConfigurableBeanFactory;
import com.echo.ioc.prop.PropertyResolver;
import com.echo.ioc.util.AnnotatedTypeMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 基于配置属性是否存在判断的Condition
 */
public class OnPropertyCondition extends AbstractCondition implements ConfigurationCondition {

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage matchMessage = ConditionMessage.empty();
        ConditionalOnProperty conditionalOnProperty = metadata.getAnnotation(ConditionalOnProperty.class);
        ConfigurableBeanFactory beanFactory = context.getBeanFactory();
        if (conditionalOnProperty != null) {
            String[] properties = conditionalOnProperty.value();
            String requiredValue = conditionalOnProperty.havingValue();
            List<String> noMatching = new ArrayList<>(properties.length);
            List<String> missing = filter(properties, beanFactory, requiredValue, noMatching);
            if (!missing.isEmpty()) {
                matchMessage = matchMessage.andCondition(ConditionalOnProperty.class)
                        .didNotFind("required propertyName", "required properties")
                        .items(ConditionMessage.Style.QUOTE, missing);
            }
            if (!noMatching.isEmpty()) {
                matchMessage = matchMessage.andCondition(ConditionalOnProperty.class)
                        .found("different value in propertyName", "different value in properties")
                        .items(ConditionMessage.Style.QUOTE, noMatching);
            }
            if (!noMatching.isEmpty() || !missing.isEmpty()) {
                return ConditionOutcome.noMatch(matchMessage);
            }
        }
        return ConditionOutcome.match(matchMessage);
    }

    private static List<String> filter(String[] properties, ConfigurableBeanFactory beanFactory, String requiredValue, List<String> noMatching) {
        if (ArrayUtil.isEmpty(properties)) {
            return Collections.emptyList();
        }
        PropertyResolver resolver = beanFactory.getPropertyResolver();
        List<String> missing = new ArrayList<>(properties.length);
        for (String propertyName : properties) {
            if (!resolver.containsProperty(propertyName)) {
                missing.add(propertyName);
            } else {
                if (!isMatch(resolver.getProperty(propertyName), requiredValue)) {
                    noMatching.add(propertyName);
                }
            }
        }
        return missing;
    }

    private static boolean isMatch(String value, String requiredValue) {
        if (StringUtils.hasLength(requiredValue)) {
            return requiredValue.equalsIgnoreCase(value);
        }
        return !"false".equalsIgnoreCase(value);
    }

}
