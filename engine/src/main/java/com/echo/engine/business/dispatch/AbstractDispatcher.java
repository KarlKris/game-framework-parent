package com.echo.engine.business.dispatch;

import com.echo.network.exception.SocketException;
import com.echo.network.message.IMessage;
import com.echo.network.message.SocketProtocol;
import com.echo.network.modules.ErrorCode;
import com.echo.network.modules.ErrorCodeModule;
import com.echo.network.modules.ServerErrorCode;
import com.echo.network.protocol.*;
import com.echo.network.session.ISession;
import com.echo.network.utils.SerializeUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * 协议分发器基类
 *
 * @author: li-yuanwen
 */
@Slf4j
public abstract class AbstractDispatcher<M extends IMessage, S extends ISession> implements Dispatcher<M, S> {


    protected final ProtocolContext protocolContext;

    public AbstractDispatcher(ProtocolContext protocolContext) {
        this.protocolContext = protocolContext;
    }

    @Override
    public void dispatch(M message, S session) {
        if (!message.isRequest()) {
            if (log.isDebugEnabled()) {
                log.debug("服务器收到非请求消息,忽略, ip:{}", session.getIp());
            }
            return;
        }

        Executor executor = findExecutor(message, session);
        executor.execute(() -> handleMessage(message, session));
    }


    /**
     * 获取执行线程
     *
     * @param message 消息
     * @param session session
     * @return Executor
     */
    protected abstract Executor findExecutor(M message, S session);


    /**
     * 协议处理
     *
     * @param session session
     * @param message message
     */
    private void handleMessage(M message, S session) {
        if (!beforeHandle(message, session)) {
            return;
        }

        byte serializeType = message.getSerializeType();
        if (!SerializeUtils.isSupported(serializeType)) {
            if (log.isWarnEnabled()) {
                log.warn("请求消息序列化类型[{}],找不到对应的序列化工具,忽略, ip:{}", serializeType, session.getIp());
            }
            return;
        }

        SocketProtocol protocol = message.getProtocol();
        if (log.isDebugEnabled()) {
            log.debug("收到消息,协议头[{}],协议号[{},{}],消息体长度[{}], ip:[{}]"
                    , message.getProtocolHeaderIdentity()
                    , protocol.getModule()
                    , protocol.getMethodId()
                    , message.getBody() == null ? 0 : message.getBody().length
                    , session.getIp());
        }
        // 记录序列化/反序列化方式
        session.setSerializeType(serializeType);
        ProtocolMethodInvocation invocation = protocolContext.getRequestInvocation(protocol);
        if (invocation == null) {
            if (log.isDebugEnabled()) {
                log.debug("不支持协议号[{},{}], ip:{}"
                        , protocol.getModule()
                        , protocol.getMethodId()
                        , session.getIp());
            }
            return;
        }

        // 检查身份标识
        final long identity = findIdentity(message, session);
        if (invocation.isIdentity() && identity == 0) {
            response(message, session, errorSocketProtocol()
                    , SerializeUtils.serialize(serializeType
                            , createExceptionBody(message.getSn(), protocol, ServerErrorCode.NO_IDENTITY)));
            return;
        }

        // 线程设置message消息
        // 将身份标识和请求序号设置进ThreadLocal,用于后续的rpc使用
        // identity可能为0,因为identity需要通过登陆或创建角色来绑定,此时的rpc协议请求应保证不会使用@Identity注解
        RequestMessageInfo.setIdentity(identity);
        RequestMessageInfo.setMessageSn(message.getSn());

        byte[] responseBody = null;
        try {
            Object result = invokeMethod(message, session, invocation);
            if (result != null) {
                if (result instanceof Future) {
                    Future<?> future = (Future<?>) result;
                    Object actualResult = future.get();
                    if (actualResult != null) {
                        responseBody = SerializeUtils.serialize(serializeType, actualResult);
                    }
                } else {
                    responseBody = SerializeUtils.serialize(serializeType, result);
                }
            }
        } catch (SocketException e) {
            protocol = errorSocketProtocol();
            responseBody = SerializeUtils.serialize(serializeType, createExceptionBody(message.getSn(), protocol, e));
        } catch (Exception e) {
            log.error("发生未知异常", e);
            protocol = errorSocketProtocol();
            responseBody = SerializeUtils.serialize(serializeType, createExceptionBody(message.getSn()
                    , protocol, ServerErrorCode.UNKNOWN));
        } finally {
            // 同步协议或有响应体时返回
            if (invocation.isSyncMethod() || responseBody != null) {
                response(message, session, protocol, responseBody);
            }

            RequestMessageInfo.removeIdentity();
            RequestMessageInfo.removeMessageSn();
        }
    }

    /**
     * 消息处理前处理,用于判断一下信息
     *
     * @param session session
     * @param message message
     * @return true 可以处理
     */
    protected boolean beforeHandle(M message, S session) {
        return true;
    }

    /**
     * 获取消息身份标识
     *
     * @param session session
     * @param message message
     * @return > 0 身份标识
     */
    protected abstract long findIdentity(M message, S session);


    /**
     * 消息处理逻辑调用
     *
     * @param session    session
     * @param message    message
     * @param invocation 调用方法上下文
     * @return method.invoke()
     */
    private Object invokeMethod(M message, S session, ProtocolMethodInvocation invocation) {
        ProtocolMethod protocolMethod = invocation.getProtocolMethod();
        MethodParameter[] params = protocolMethod.getParams();
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            MethodParameter parameters = params[i];
            if (parameters instanceof SessionMethodParameter) {
                args[i] = session;
                continue;
            }

            if (parameters instanceof IdentityMethodParameter) {
                args[i] = findIdentity(message, session);
                continue;
            }

            if (parameters instanceof InBodyMethodParameter) {
                args[i] = SerializeUtils.deserialize(message.getSerializeType()
                        , message.getBody(), parameters.getParameterClass());
                continue;
            }

            // 理论上不会运行到这行代码
            log.warn("业务消息参数解析出现未允许出现的参数类型,参数类型[{}-{}]"
                    , parameters.getClass().getSimpleName(), parameters.getParameterClass());

        }
        return invocation.invokeMethod(args);
    }

    /**
     * 对消息message进行响应
     *
     * @param session      session
     * @param message      msg
     * @param protocol     协议号
     * @param responseBody 协议号对应的消息体
     */
    protected abstract void response(M message, S session, SocketProtocol protocol, byte[] responseBody);


    private SocketProtocol errorSocketProtocol() {
        return ErrorCodeModule.ERROR_CODE_RESPONSE;
    }


    /**
     * 封装异常成消息体
     *
     * @param exception 异常
     * @return /
     */
    private Object createExceptionBody(long reqSn, SocketProtocol protocol, SocketException exception) {
        return createExceptionBody(reqSn, protocol, exception.getErrorCode());
    }

    /**
     * 封装异常码成消息体
     *
     * @param code 异常码
     * @return /
     */
    private Object createExceptionBody(long reqSn, SocketProtocol protocol, int code) {
        return new ErrorCode(reqSn, protocol, code);
    }
}
