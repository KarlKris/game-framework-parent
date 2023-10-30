package com.echo.autoconfigure.framework;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.ClassScanner;
import com.echo.network.anno.SocketController;
import com.echo.network.anno.SocketPush;
import com.echo.network.anno.SocketResponse;
import com.echo.network.message.SocketProtocol;
import com.echo.network.protocol.ProtocolContext;
import com.echo.network.protocol.ProtocolMethod;
import com.echo.network.utils.ProtocolUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author: li-yuanwen
 */
public class ProtocolScanner {


    public static void scan(String protocolPackage, ProtocolContext protocolContext) {
        Map<SocketProtocol, Class<?>> responseTypeMap = new HashMap<>();
        for (Class<?> targetClass : new ClassScanner(protocolPackage).scan()) {
            if (!targetClass.isInterface()) {
                SocketResponse annotation = AnnotationUtil.getAnnotation(targetClass, SocketResponse.class);
                if (annotation == null) {
                    continue;
                }
                Class<?> oldClass = responseTypeMap.putIfAbsent(new SocketProtocol(annotation.module(), annotation.id()), targetClass);
                if (oldClass != null) {
                    throw new IllegalStateException("same socket protocol, module:" + annotation.module() + " id:" + annotation.id()
                            + " " + oldClass.getName() + " and " + targetClass.getName());
                }
            } else {
                boolean hasAnnotation = AnnotationUtil.hasAnnotation(targetClass, SocketController.class);
                if (!hasAnnotation) {
                    continue;
                }

                List<ProtocolMethod> list;
                if (AnnotationUtil.hasAnnotation(targetClass, SocketPush.class)) {
                    list = ProtocolUtils.findProtocolPushMethodList(targetClass);
                } else {
                    list = ProtocolUtils.findProtocolMethodList(targetClass);
                }

                list.forEach(protocolContext::registerProtocol);
            }
        }
        // 协议响应对象检查
        for (Map.Entry<SocketProtocol, Class<?>> entry : responseTypeMap.entrySet()) {
            ProtocolMethod protocolMethod = protocolContext.getProtocol(entry.getKey());
            if (protocolMethod.isSyncMethod()) {
                Method method = protocolMethod.getMethod();
                Class<?> returnClz = method.getReturnType();
                if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
                    returnClz = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                }
                if (!entry.getValue().isAssignableFrom(returnClz)) {
                    throw new IllegalStateException("协议号[" + entry.getKey() + "]的返回对象类型注解非法");
                }
            }
            protocolMethod.setReturnClz(entry.getValue());
        }
    }

}
