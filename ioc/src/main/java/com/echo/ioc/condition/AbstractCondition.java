package com.echo.ioc.condition;

import com.echo.common.util.ClassUtils;
import com.echo.common.util.StringUtils;
import com.echo.ioc.util.AnnotatedTypeMetadata;
import com.echo.ioc.util.StandardClassMetadata;
import com.echo.ioc.util.StandardMethodMetadata;
import lombok.extern.slf4j.Slf4j;

/**
 * Condition 基类
 */
@Slf4j
public abstract class AbstractCondition implements Condition {

    @Override
    public boolean match(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String classOrMethodName = getClassOrMethodName(metadata);

        ConditionOutcome outcome = getMatchOutcome(context, metadata);
        logOutcome(classOrMethodName, outcome);
        return outcome.isMatch();
    }

    private static String getClassOrMethodName(AnnotatedTypeMetadata metadata) {
        if (metadata instanceof StandardClassMetadata) {
            return ((StandardClassMetadata) metadata).getClassName();
        }
        StandardMethodMetadata methodMetadata = (StandardMethodMetadata) metadata;
        return methodMetadata.getDeclaringClassName() + "#" + methodMetadata.getMethodName();
    }

    /**
     * Determine the outcome of the match along with suitable log output.
     * @param context the condition context
     * @param metadata the annotation metadata
     * @return the condition outcome
     */
    public abstract ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata);

    protected final void logOutcome(String classOrMethodName, ConditionOutcome outcome) {
        if (log.isTraceEnabled()) {
            log.trace(getLogMessage(classOrMethodName, outcome));
        }
    }

    private String getLogMessage(String classOrMethodName, ConditionOutcome outcome) {
        StringBuilder message = new StringBuilder();
        message.append("Condition ");
        message.append(ClassUtils.getShortName(getClass()));
        message.append(" on ");
        message.append(classOrMethodName);
        message.append(outcome.isMatch() ? " matched" : " did not match");
        if (StringUtils.hasLength(outcome.getMessage())) {
            message.append(" due to ");
            message.append(outcome.getMessage());
        }
        return message.toString();
    }


    /**
     * Slightly faster variant of {@link ClassUtils#forName(String, ClassLoader)} that
     * doesn't deal with primitives, arrays or inner types.
     * @param className the class name to resolve
     * @param classLoader the class loader to use
     * @return a resolved class
     * @throws ClassNotFoundException if the class cannot be found
     */
    protected static Class<?> resolve(String className, ClassLoader classLoader) throws ClassNotFoundException {
        if (classLoader != null) {
            return Class.forName(className, false, classLoader);
        }
        return Class.forName(className);
    }
}
