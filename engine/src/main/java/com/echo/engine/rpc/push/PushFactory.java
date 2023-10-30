package com.echo.engine.rpc.push;

import cn.hutool.core.annotation.AnnotationUtil;
import com.echo.network.anno.SocketPush;
import com.echo.network.protocol.ProtocolContext;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * 推送对象代理工厂
 *
 * @author: li-yuanwen
 */
public class PushFactory {

    private final PushOperation pushOperation;

    private final ProtocolContext protocolContext;

    /**
     * 代理对象
     **/
    private final Map<String, Object> innerProxy = new HashMap<>();
    private final Map<String, Object> outerProxy = new HashMap<>();

    public PushFactory(PushOperation pushOperation, ProtocolContext protocolContext) {
        this.pushOperation = pushOperation;
        this.protocolContext = protocolContext;
    }

    /**
     * 获取InnerMessage消息推送代理
     *
     * @param clz
     * @param <T>
     * @return
     */
    public <T> T getInnerPushProxy(Class<T> clz) {
        String name = clz.getName();
        Object target = this.innerProxy.get(name);
        if (target != null) {
            return (T) target;
        }
        synchronized (this.innerProxy) {
            target = this.innerProxy.get(name);
            if (target != null) {
                return (T) target;
            }

            // 非接口
            if (!clz.isInterface()) {
                throw new RuntimeException(clz.getSimpleName() + "不是推送接口");
            }

            SocketPush socketPush = AnnotationUtil.getAnnotation(clz, SocketPush.class);
            if (socketPush == null) {
                throw new RuntimeException(clz.getSimpleName() + "不是推送接口");
            }


            target = Proxy.newProxyInstance(clz.getClassLoader()
                    , new Class[]{clz}
                    , new InnerMsgPushProxyInvoker(pushOperation, protocolContext));

            this.innerProxy.put(name, target);
        }
        return (T) target;
    }


    /**
     * 获取OuterMessage消息推送代理
     *
     * @param clz
     * @param <T>
     * @return
     */
    public <T> T getOuterPushProxy(Class<T> clz) {
        String name = clz.getName();
        Object target = this.outerProxy.get(name);
        if (target != null) {
            return (T) target;
        }
        synchronized (this.outerProxy) {
            target = this.outerProxy.get(name);
            if (target != null) {
                return (T) target;
            }

            // 非接口
            if (!clz.isInterface()) {
                throw new RuntimeException(clz.getSimpleName() + "不是推送接口");
            }

            SocketPush socketPush = AnnotationUtil.getAnnotation(clz, SocketPush.class);
            if (socketPush == null) {
                throw new RuntimeException(clz.getSimpleName() + "不是推送接口");
            }

            target = Proxy.newProxyInstance(clz.getClassLoader()
                    , new Class[]{clz}
                    , new OuterMsgPushProxyInvoker(pushOperation, protocolContext));

            this.outerProxy.put(name, target);
        }
        return (T) target;
    }

}
