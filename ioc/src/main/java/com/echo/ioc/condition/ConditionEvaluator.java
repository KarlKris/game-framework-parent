package com.echo.ioc.condition;

import com.echo.common.util.ObjectUtils;
import com.echo.ioc.anno.Conditional;
import com.echo.ioc.loader.BeanDefinitionRegistry;
import com.echo.ioc.util.AnnotatedTypeMetadata;
import com.echo.ioc.condition.ConfigurationCondition.ConfigurationPhase;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link com.echo.ioc.anno.Conditional} 计算 {@link Condition} 结果
 */
public class ConditionEvaluator {

    private final ConditionContext context;

    public ConditionEvaluator(BeanDefinitionRegistry registry) {
        this.context = new ConditionContext(registry);
    }

    public boolean shouldSkip(AnnotatedTypeMetadata metadata) {
        return shouldSkip(metadata, null);
    }

    public boolean shouldSkip(AnnotatedTypeMetadata metadata, ConfigurationPhase phase) {
        if (metadata == null || !metadata.hasAnnotation(Conditional.class)) {
            return false;
        }

        List<Condition> conditions = new ArrayList<>(1);
        Conditional conditional = metadata.getAnnotation(Conditional.class);
        for (Class<? extends Condition> conditionClz : conditional.value()) {
            conditions.add(ObjectUtils.newInstance(conditionClz));
        }

        for (Condition condition : conditions) {
            ConfigurationPhase requiredPhase = null;
            if (condition instanceof ConfigurationCondition) {
                requiredPhase = ((ConfigurationCondition) condition).getConfigurationPhase();
            }
            if ((requiredPhase == null || requiredPhase == phase) && !condition.match(context, metadata)) {
                return true;
            }
        }

        return false;
    }

}
