package com.echo.mongo;

import cn.hutool.core.lang.Assert;
import com.echo.common.concurrency.ConcurrentReferenceHashMap;
import com.echo.common.util.ClassUtils;
import com.echo.common.util.MethodClassKey;
import com.echo.common.util.ReflectionUtils;
import com.mongodb.session.ClientSession;
import javassist.util.proxy.MethodHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * javassist 代理
 * @author: li-yuanwen
 */
public class SessionAwareMethodHandler<D, C> implements MethodHandler {

    private static final MethodCache METHOD_CACHE = new MethodCache();

    private final ClientSession session;
    private final ClientSessionOperator<C> collectionDecorator;
    private final ClientSessionOperator<D> databaseDecorator;
    private final Object target;
    private final Class<?> targetType;
    private final Class<?> collectionType;
    private final Class<?> databaseType;
    private final Class<? extends ClientSession> sessionType;

    /**
     * Create a new SessionAwareMethodInterceptor for given target.
     *
     * @param session the {@link ClientSession} to be used on invocation.
     * @param target the original target object.
     * @param databaseType the MongoDB database type
     * @param databaseDecorator a {@link ClientSessionOperator} used to create the proxy for an imperative / reactive
     *          {@code MongoDatabase}.
     * @param collectionType the MongoDB collection type.
     * @param collectionDecorator a {@link ClientSessionOperator} used to create the proxy for an imperative / reactive
     *          {@code MongoCollection}.
     * @param <T> target object type.
     */
    public <T> SessionAwareMethodHandler(ClientSession session, T target, Class<? extends ClientSession> sessionType,
                                             Class<D> databaseType, ClientSessionOperator<D> databaseDecorator, Class<C> collectionType,
                                             ClientSessionOperator<C> collectionDecorator) {

        Assert.notNull(session, "ClientSession must not be null");
        Assert.notNull(target, "Target must not be null");
        Assert.notNull(sessionType, "SessionType must not be null");
        Assert.notNull(databaseType, "Database type must not be null");
        Assert.notNull(databaseDecorator, "Database ClientSessionOperator must not be null");
        Assert.notNull(collectionType, "Collection type must not be null");
        Assert.notNull(collectionDecorator, "Collection ClientSessionOperator must not be null");



        this.session = session;
        this.target = target;
        this.databaseType = ClassUtils.getUserClass(databaseType);
        this.collectionType = ClassUtils.getUserClass(collectionType);
        this.collectionDecorator = collectionDecorator;
        this.databaseDecorator = databaseDecorator;

        this.targetType = ClassUtils.isAssignable(databaseType, target.getClass()) ? databaseType : collectionType;
        this.sessionType = sessionType;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {

        if (requiresDecoration(thisMethod)) {

            Object target = thisMethod.invoke(this.target, args);
            if (target instanceof Proxy) {
                return target;
            }

            return decorate(target);
        }

        if (!requiresSession(thisMethod)) {
            return thisMethod.invoke(this.target, args);
        }

        Optional<Method> targetMethod = METHOD_CACHE.lookup(thisMethod, targetType, sessionType);

        return !targetMethod.isPresent() ? thisMethod.invoke(this.target, args)
                : ReflectionUtils.invokeMethod(targetMethod.get(), target,
                prependSessionToArguments(session, args));
    }

    private boolean requiresDecoration(Method method) {

        return ClassUtils.isAssignable(databaseType, method.getReturnType())
                || ClassUtils.isAssignable(collectionType, method.getReturnType());
    }

    @SuppressWarnings("unchecked")
    protected Object decorate(Object target) {

        return ClassUtils.isAssignable(databaseType, target.getClass()) ? databaseDecorator.apply(session, (D) target)
                : collectionDecorator.apply(session, (C) target);
    }

    private static boolean requiresSession(Method method) {

        if (method.getParameterCount() == 0
                || !ClassUtils.isAssignable(ClientSession.class, method.getParameterTypes()[0])) {
            return true;
        }

        return false;
    }

    private static Object[] prependSessionToArguments(ClientSession session, Object[] args) {

        Object[] array = new Object[args.length + 1];

        array[0] = session;
        System.arraycopy(args, 0, array, 1, args.length);

        return args;
    }

    /**
     * Simple {@link Method} to {@link Method} caching facility for {@link ClientSession} overloaded targets.
     *
     * @since 2.1
     * @author Christoph Strobl
     */
    static class MethodCache {

        private final ConcurrentReferenceHashMap<MethodClassKey, Optional<Method>> cache = new ConcurrentReferenceHashMap<>();

        /**
         * Lookup the target {@link Method}.
         *
         * @param method
         * @param targetClass
         * @return
         */
        Optional<Method> lookup(Method method, Class<?> targetClass, Class<? extends ClientSession> sessionType) {

            return cache.computeIfAbsent(new MethodClassKey(method, targetClass),
                    val -> Optional.ofNullable(findTargetWithSession(method, targetClass, sessionType)));
        }

        private Method findTargetWithSession(Method sourceMethod, Class<?> targetType,
                                             Class<? extends ClientSession> sessionType) {

            Class<?>[] argTypes = sourceMethod.getParameterTypes();
            Class<?>[] args = new Class<?>[argTypes.length + 1];
            args[0] = sessionType;
            System.arraycopy(argTypes, 0, args, 1, argTypes.length);
            return ReflectionUtils.getMethod(targetType, sourceMethod.getName(), args);
        }

        /**
         * Check whether the cache contains an entry for {@link Method} and {@link Class}.
         *
         * @param method
         * @param targetClass
         * @return
         */
        boolean contains(Method method, Class<?> targetClass) {
            return cache.containsKey(new MethodClassKey(method, targetClass));
        }
    }

    /**
     * Represents an operation upon two operands of the same type, producing a result of the same type as the operands
     * accepting {@link ClientSession}. This is a specialization of {@link BiFunction} for the case where the operands and
     * the result are all of the same type.
     *
     * @param <T> the type of the operands and result of the operator
     */
    public interface ClientSessionOperator<T> extends BiFunction<ClientSession, T, T> {}
}
