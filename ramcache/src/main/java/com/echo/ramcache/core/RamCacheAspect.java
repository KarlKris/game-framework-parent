package com.echo.ramcache.core;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ArrayUtil;
import com.echo.common.convert.core.StandardReflectionParameterNameDiscoverer;
import com.echo.common.expression.Jexl3;
import com.echo.ramcache.anno.Cacheable;
import com.echo.ramcache.anno.CachedEvict;
import com.echo.ramcache.anno.CachedPut;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Cacheable},{@link com.echo.ramcache.anno.CachedEvict}
 * 和{@link com.echo.ramcache.anno.CachedPut}切面
 *
 * @author: li-yuanwen
 */
@Aspect
public class RamCacheAspect {

    public static final String PREFIX = "#";

    private final static Logger log = LoggerFactory.getLogger(RamCacheAspect.class);

    private final static StandardReflectionParameterNameDiscoverer DISCOVERER = new StandardReflectionParameterNameDiscoverer();


    /**
     * 缓存移除
     **/
    @Before("@annotation(com.echo.ramcache.anno.CachedEvict) && execution(* *(..))")
    public void beforeInvoke(JoinPoint jp) throws NoSuchMethodException, JsonProcessingException {
        Method targetMethod = getTargetMethod(jp);
        CachedEvict cachedEvict = AnnotationUtil.getAnnotation(targetMethod, CachedEvict.class);

        Map<String, Object> context = null;
        String cacheName = cachedEvict.name();
        if (cacheName.startsWith(PREFIX)) {
            Object[] args = jp.getArgs();
            context = buildExpressionContext(targetMethod, args);
            cacheName = getExpressionValue(cacheName.substring(1), context);
        }

        // 移除缓存
        CaffeineCache caffeineCache = CacheFactory.getCache(cacheName);
        if (caffeineCache != null) {
            String key = cachedEvict.key();
            if (key.startsWith(PREFIX)) {
                if (context == null) {
                    Object[] args = jp.getArgs();
                    context = buildExpressionContext(targetMethod, args);
                }
                key = getExpressionValue(key.substring(1), context);
            }
            caffeineCache.remove(key);

            if (log.isInfoEnabled()) {
                log.info("@CachedEvict method:{}, cacheName:{}, key:{} ", targetMethod.getName(), cacheName, key);
            }
        }
    }

    /**
     * 缓存更新
     **/
    @AfterReturning(value = "@annotation(com.echo.ramcache.anno.CachedPut) && execution(* *(..))", returning = "result")
    public void afterUpdate(JoinPoint jp, Object result) throws NoSuchMethodException, JsonProcessingException {
        Method targetMethod = getTargetMethod(jp);
        CachedPut cachedPut = AnnotationUtil.getAnnotation(targetMethod, CachedPut.class);

        Map<String, Object> context = null;
        String cacheName = cachedPut.name();
        if (cacheName.startsWith(PREFIX)) {
            Object[] args = jp.getArgs();
            context = buildExpressionContext(targetMethod, args);
            cacheName = getExpressionValue(cacheName.substring(1), context);
        }

        CaffeineCache caffeineCache = CacheFactory.computeIfAbsent(cacheName
                , new CaffeineCacheBuilder(cachedPut.maximum(), cachedPut.expire()));

        String key = cachedPut.key();
        if (key.startsWith(PREFIX)) {
            if (context == null) {
                Object[] args = jp.getArgs();
                context = buildExpressionContext(targetMethod, args);
            }
            key = getExpressionValue(key.substring(1), context);
        }

        caffeineCache.put(key, result);

        if (log.isInfoEnabled()) {
            log.info("@CachedPut method:{}, cacheName:{} key:{}", targetMethod.getName(), cacheName, key);
        }
    }

    /**
     * 查询缓存
     **/
    @Around("@annotation(com.echo.ramcache.anno.Cacheable) && execution(* *(..))")
    public Object aroundInvoke(ProceedingJoinPoint joinPoint) throws NoSuchMethodException, JsonProcessingException {
        Method targetMethod = getTargetMethod(joinPoint);
        Cacheable cacheable = AnnotationUtil.getAnnotation(targetMethod, Cacheable.class);

        Map<String, Object> context = null;
        String cacheName = cacheable.name();
        if (cacheName.startsWith(PREFIX)) {
            Object[] args = joinPoint.getArgs();
            context = buildExpressionContext(targetMethod, args);
            cacheName = getExpressionValue(cacheName.substring(1), context);
        }

        String key = cacheable.key();
        if (key.startsWith(PREFIX)) {
            if (context == null) {
                Object[] args = joinPoint.getArgs();
                context = buildExpressionContext(targetMethod, args);
            }
            key = getExpressionValue(key.substring(1), context);
        }
        CaffeineCache caffeineCache = CacheFactory.computeIfAbsent(cacheName
                , new CaffeineCacheBuilder(cacheable.maximum(), cacheable.expire()));

        Object result = null;
        Class<?> returnType = targetMethod.getReturnType();
        if ((result = caffeineCache.get(key, returnType)) == null) {
            try {

                if (log.isInfoEnabled()) {
                    log.info("@Cacheable method:{}, cacheName:{} key:{} not found", targetMethod.getName(), cacheName, key);
                }

                result = joinPoint.proceed();
                if (cacheable.nullCache() || result != null) {
                    caffeineCache.put(key, result);
                }
                return result;
            } catch (Throwable throwable) {
                log.error("执行方法[{}],方法参数[{}]出现未知异常", targetMethod.getName(), joinPoint.getArgs(), throwable);
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("@Cacheable method:{}, cacheName:{} key:{} found", targetMethod.getName(), cacheName, key);
            }
        }
        return result;
    }


    /**
     * 获取当前执行的方法
     *
     * @param pjp
     * @return
     * @throws NoSuchMethodException
     */
    private Method getTargetMethod(JoinPoint pjp) throws NoSuchMethodException {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();
        return pjp.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
    }


    private Map<String, Object> buildExpressionContext(Method method, Object[] args) {
        if (ArrayUtil.isEmpty(args)) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new HashMap<>(args.length);
        String[] parameterNames = DISCOVERER.getParameterNames(method);
        for (int len = 0; len < parameterNames.length; len++) {
            map.put(parameterNames[len], args[len]);
        }
        return map;
    }


    private String getExpressionValue(String expression, Map<String, Object> context) {
        return Jexl3.eval(expression, context, String.class);
    }
}
