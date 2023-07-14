package com.echo.ioc.condition;

import cn.hutool.core.collection.CollectionUtil;
import com.echo.common.util.ClassUtils;
import com.echo.ioc.anno.ConditionalOnClass;
import com.echo.ioc.util.AnnotatedTypeMetadata;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于Class加载判断的Condition
 */
public class OnClassCondition extends AbstractCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
        ConditionMessage matchMessage = ConditionMessage.empty();
        ConditionalOnClass conditionalOnClass = metadata.getAnnotation(ConditionalOnClass.class);
        if (conditionalOnClass != null) {
            List<String> onClasses = Arrays.stream(conditionalOnClass.value()).collect(Collectors.toList());
            List<String> missing = filter(onClasses, ClassNameFilter.MISSING, classLoader);
            if (!missing.isEmpty()) {
                return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnClass.class)
                        .didNotFind("required class", "required classes")
                        .items(ConditionMessage.Style.QUOTE, missing));
            }
            matchMessage = matchMessage.andCondition(ConditionalOnClass.class)
                    .found("required class", "required classes")
                    .items(ConditionMessage.Style.QUOTE, filter(onClasses, ClassNameFilter.PRESENT, classLoader));
        }
        return ConditionOutcome.match(matchMessage);
    }

    protected final List<String> filter(Collection<String> classNames, ClassNameFilter classNameFilter,
                                        ClassLoader classLoader) {
        if (CollectionUtil.isEmpty(classNames)) {
            return Collections.emptyList();
        }
        List<String> matches = new ArrayList<>(classNames.size());
        for (String candidate : classNames) {
            if (classNameFilter.matches(candidate, classLoader)) {
                matches.add(candidate);
            }
        }
        return matches;
    }

    protected enum ClassNameFilter {

        PRESENT {

            @Override
            public boolean matches(String className, ClassLoader classLoader) {
                return isPresent(className, classLoader);
            }

        },

        MISSING {

            @Override
            public boolean matches(String className, ClassLoader classLoader) {
                return !isPresent(className, classLoader);
            }

        };

        abstract boolean matches(String className, ClassLoader classLoader);

        static boolean isPresent(String className, ClassLoader classLoader) {
            if (classLoader == null) {
                classLoader = ClassUtils.getDefaultClassLoader();
            }
            try {
                resolve(className, classLoader);
                return true;
            }
            catch (Throwable ex) {
                return false;
            }
        }
    }

}
