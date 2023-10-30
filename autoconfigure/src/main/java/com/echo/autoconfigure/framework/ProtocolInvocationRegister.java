package com.echo.autoconfigure.framework;

import cn.hutool.core.annotation.AnnotationUtil;
import com.echo.ioc.exception.BeansException;
import com.echo.ioc.processor.InstantiationAwareBeanPostProcessor;
import com.echo.network.anno.SocketController;
import com.echo.network.anno.SocketPush;
import com.echo.network.protocol.ProtocolContext;
import com.echo.network.protocol.ProtocolMethod;
import com.echo.network.protocol.ProtocolMethodInvocation;
import com.echo.network.utils.ProtocolUtils;

import java.util.List;

/**
 * 协议Invocation注册
 *
 * @author: li-yuanwen
 */
public class ProtocolInvocationRegister implements InstantiationAwareBeanPostProcessor {

    private final ProtocolContext protocolContext;

    public ProtocolInvocationRegister(ProtocolContext protocolContext) {
        this.protocolContext = protocolContext;
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        boolean hasAnnotation = AnnotationUtil.hasAnnotation(beanClass, SocketController.class);
        if (!hasAnnotation) {
            return bean;
        }

        List<ProtocolMethod> list;
        if (AnnotationUtil.hasAnnotation(beanClass, SocketPush.class)) {
            list = ProtocolUtils.findProtocolPushMethodList(beanClass);
        } else {
            list = ProtocolUtils.findProtocolMethodList(beanClass);
        }

        for (ProtocolMethod protocolMethod : list) {
            ProtocolMethod protocol = protocolContext.getProtocol(protocolMethod.getProtocol());
            ProtocolMethodInvocation invocation = new ProtocolMethodInvocation(bean, protocol);
            if (!protocolContext.registerRequestProtocol(invocation)) {
                throw new IllegalStateException("出现相同协议号["
                        + protocolMethod.getProtocol()
                        + "]");
            }
        }

        return bean;
    }
}
