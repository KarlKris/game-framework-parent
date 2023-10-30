package com.echo.network.protocol;

import com.echo.network.message.SocketProtocol;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 协议容器
 *
 * @author: li-yuanwen
 */
public class ProtocolContext {

    /**
     * 全局协议
     **/
    private final Map<SocketProtocol, ProtocolMethod> protocols = new HashMap<>();

    // -------------------------------------------------------------------------------------------------

    /**
     * 请求-响应协议
     **/
    private final Map<SocketProtocol, ProtocolMethodInvocation> requestProtocols = new HashMap<>();
    /**
     * 推送协议
     **/
    private final Map<Method, ProtocolMethod> pushProtocols = new HashMap<>();

    public void registerProtocol(ProtocolMethod protocolMethod) {
        protocols.put(protocolMethod.getProtocol(), protocolMethod);
    }

    public boolean registerRequestProtocol(ProtocolMethodInvocation invocation) {
        return requestProtocols.putIfAbsent(invocation.getProtocolMethod().getProtocol(), invocation) == null;
    }

    public void registerPushProtocol(ProtocolMethod protocolMethod) {
        pushProtocols.put(protocolMethod.getMethod(), protocolMethod);
    }

    public ProtocolMethodInvocation getRequestInvocation(SocketProtocol protocol) {
        return requestProtocols.get(protocol);
    }

    public ProtocolMethod getPushProtocolByMethod(Method method) {
        return pushProtocols.get(method);
    }

    public ProtocolMethod getProtocol(SocketProtocol protocol) {
        return protocols.get(protocol);
    }
}
