package com.echo.engine.rpc.push;

import com.echo.common.util.ObjectUtils;
import com.echo.engine.boostrap.NettyServerBootstrap;
import com.echo.network.message.PushResponse;
import com.echo.network.protocol.*;
import com.echo.network.session.ISession;
import com.echo.network.utils.SerializeUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;

/**
 * InnerMessage推送代理
 *
 * @author: li-yuanwen
 */
public class InnerMsgPushProxyInvoker implements InvocationHandler {

    private final PushOperation pushOperation;

    private final ProtocolContext protocolContext;

    public InnerMsgPushProxyInvoker(PushOperation pushOperation, ProtocolContext protocolContext) {
        this.pushOperation = pushOperation;
        this.protocolContext = protocolContext;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (ObjectUtils.OBJECT_METHODS.contains(method)) {
            return method.invoke(proxy, args);
        }

        ProtocolMethod protocolMethod = protocolContext.getPushProtocolByMethod(method);
        if (protocolMethod == null) {
            throw new RuntimeException("推送方法[" + method.getName() + "]没有添加 @SocketPush 注解");
        }

        MethodParameter[] params = protocolMethod.getParams();
        byte[] content = null;
        Collection<Long> targets = Collections.emptyList();
        for (int i = 0; i < args.length; i++) {
            if (params[i] instanceof InBodyMethodParameter) {
                content = SerializeUtils.serialize(NettyServerBootstrap.SERIALIZE_TYPE, args[i]);
                continue;
            }

            if (params[i] instanceof PushIdsMethodParameter) {
                targets = (Collection<Long>) args[i];
            }
        }

        if (targets.isEmpty()) {
            return null;
        }

        // 构建每个Channel需要发送的目标
        Map<ISession, Set<Long>> session2Identities = new HashMap<>(targets.size());
        for (long identity : targets) {
            ISession session = pushOperation.getSessionContext().getIdentitySession(identity);
            if (session == null) {
                continue;
            }
            session2Identities.computeIfAbsent(session, k -> new HashSet<>()).add(identity);
        }

        for (Map.Entry<ISession, Set<Long>> entry : session2Identities.entrySet()) {
            PushResponse response = new PushResponse(entry.getValue(), content);
            pushOperation.pushInnerMessage(entry.getKey(), response, protocolMethod.getProtocol());
        }

        return null;
    }
}
