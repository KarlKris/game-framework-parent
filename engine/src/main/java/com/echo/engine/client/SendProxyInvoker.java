package com.echo.engine.client;

import com.echo.common.util.IpUtils;
import com.echo.common.util.ObjectUtils;
import com.echo.engine.boostrap.NettyServerBootstrap;
import com.echo.engine.business.LocalMessageContext;
import com.echo.engine.protocol.MessageFactory;
import com.echo.engine.rpc.core.InvocationContext;
import com.echo.engine.rpc.core.RpcInvocation;
import com.echo.network.exception.BadRequestException;
import com.echo.network.exception.SocketException;
import com.echo.network.message.InnerMessage;
import com.echo.network.message.ProtocolConstant;
import com.echo.network.modules.ServerErrorCode;
import com.echo.network.protocol.InBodyMethodParameter;
import com.echo.network.protocol.MethodParameter;
import com.echo.network.protocol.ProtocolContext;
import com.echo.network.protocol.ProtocolMethod;
import com.echo.network.utils.SerializeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author li-yuanwen
 * rpc代理实际执行器
 */
@Slf4j
public class SendProxyInvoker implements InvocationHandler {

    /**
     * 连接对方的Client
     **/
    private final NettyClient client;
    /**
     * 远程调用消息Future容器
     **/
    private final InvocationContext invocationContext;
    /**
     * 协议管理器
     **/
    private final ProtocolContext protocolContext;
    /**
     * 消息工厂
     **/
    private final MessageFactory messageFactory;
    /**
     * 超时时间(秒)
     **/
    private final int timeoutSecond;


    public SendProxyInvoker(NettyClient client, InvocationContext invocationContext
            , ProtocolContext protocolContext
            , MessageFactory messageFactory
            , int timeoutSecond) {
        this.client = client;
        this.invocationContext = invocationContext;
        this.protocolContext = protocolContext;
        this.messageFactory = messageFactory;
        this.timeoutSecond = timeoutSecond;

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (ObjectUtils.OBJECT_METHODS.contains(method)) {
            return method.invoke(proxy, args);
        }

        ProtocolMethod protocolMethod = protocolContext.getPushProtocolByMethod(method);
        if (protocolMethod == null) {
            throw new IllegalArgumentException("远程方法[" + method.getName() + "]没有添加 @SocketCommand 注解");
        }

        byte[] body = null;
        MethodParameter[] params = protocolMethod.getParams();
        for (int i = 0; i < args.length; i++) {
            if (params[i] instanceof InBodyMethodParameter) {
                body = SerializeUtils.serialize(NettyServerBootstrap.SERIALIZE_TYPE, params[i]);
                break;
            }
        }

        final Long messageSn = LocalMessageContext.getMessageSn();
        final Long identity = LocalMessageContext.getIdentity();
        InnerMessage message = messageFactory.toInnerMessage(invocationContext.nextSn()
                , ProtocolConstant.BUSINESS_REQ
                , protocolMethod.getProtocol()
                , body
                , identity
                , IpUtils.getLocalIpAddress());

        try {
            Class<?> returnType = method.getReturnType();
            boolean sync = !returnType.isAssignableFrom(CompletableFuture.class);
            RpcInvocation rpcInvocation = new RpcInvocation(message.getSn(), messageSn, identity, sync
                    , protocolContext, new CompletableFuture<>());

            client.send(message, rpcInvocation);

            if (!sync) {
                return rpcInvocation.getFuture();
            }

            return rpcInvocation.getFuture().get(timeoutSecond, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            log.error("SendProxyInvoker超时中断", e);
            throw new SocketException(ServerErrorCode.TIME_OUT);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof BadRequestException) {
                throw (BadRequestException) cause;
            }
            if (cause instanceof SocketException) {
                throw (SocketException) cause;
            }
            log.error("SendProxyInvoker发生未知ExecutionException异常", e);
            throw new SocketException(ServerErrorCode.UNKNOWN);
        } catch (Exception e) {
            log.error("SendProxyInvoker发生未知异常", e);
            throw new SocketException(ServerErrorCode.UNKNOWN);
        }
    }
}
