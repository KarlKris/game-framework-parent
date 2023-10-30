package com.echo.autoconfigure.framework;

import com.echo.common.util.ReflectionUtils;
import com.echo.engine.rpc.push.PushFactory;
import com.echo.ioc.exception.BeansException;
import com.echo.ioc.processor.InstantiationAwareBeanPostProcessor;
import org.slf4j.helpers.MessageFormatter;

import java.lang.reflect.Field;

/**
 * {@link PushInject} 推送注入实现
 *
 * @author: li-yuanwen
 */
public class PushInjectProcessor implements InstantiationAwareBeanPostProcessor {

    private final PushFactory pushFactory;

    public PushInjectProcessor(PushFactory pushFactory) {
        this.pushFactory = pushFactory;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        for (Field field : ReflectionUtils.getFields(bean.getClass()
                , field -> field.getAnnotation(PushInject.class) != null)) {
            Object pushProxy;
            PushInject annotation = field.getAnnotation(PushInject.class);
            if (annotation.outerMessage()) {
                pushProxy = pushFactory.getOuterPushProxy(field.getType());
            } else {
                pushProxy = pushFactory.getInnerPushProxy(field.getType());
            }
            inject(bean, field, pushProxy);
        }
        return true;
    }

    /**
     * 属性注入
     **/
    private void inject(Object bean, Field field, Object value) {
        ReflectionUtils.makeAccessible(field);
        try {
            field.set(bean, value);
        } catch (IllegalAccessException e) {
            String message = MessageFormatter.format("beanClass:{} 属性:{} 注入失败"
                    , bean.getClass().getName(), field.getName()).getMessage();
            throw new RuntimeException(message);
        }
    }
}
