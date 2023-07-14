package com.echo.ioc.condition;

import cn.hutool.core.util.ArrayUtil;
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
public class OnPropertyCondition extends AbstractCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage matchMessage = ConditionMessage.empty();
        ConditionalOnProperty conditionalOnProperty = metadata.getAnnotation(ConditionalOnProperty.class);
        ConfigurableBeanFactory beanFactory = context.getBeanFactory();
        if (conditionalOnProperty != null) {
            String[] properties = conditionalOnProperty.value();
            List<String> missing = filter(properties, beanFactory, false);
            if (!missing.isEmpty()) {
                return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnProperty.class)
                        .didNotFind("required propertyName", "required properties")
                        .items(ConditionMessage.Style.QUOTE, missing));
            }
            matchMessage = matchMessage.andCondition(ConditionalOnProperty.class)
                    .found("required propertyName", "required properties")
                    .items(ConditionMessage.Style.QUOTE, filter(properties, beanFactory, true));
        }
        return ConditionOutcome.match(matchMessage);
    }

    private static List<String> filter(String[] properties, ConfigurableBeanFactory beanFactory, boolean contain) {
        if (ArrayUtil.isEmpty(properties)) {
            return Collections.emptyList();
        }
        PropertyResolver propertyResolver = beanFactory.getPropertyResolver();
        List<String> list = new ArrayList<>(properties.length);
        for (String propertyName : properties) {
            if (propertyResolver.containsProperty(propertyName) && contain) {
                list.add(propertyName);
            } else if (!contain) {
                list.add(propertyName);
            }
        }
        return list;
    }

}
