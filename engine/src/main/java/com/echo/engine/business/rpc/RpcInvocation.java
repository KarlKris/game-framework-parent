package com.echo.engine.business.rpc;

import cn.hutool.core.util.ArrayUtil;
import com.echo.network.exception.SocketException;
import com.echo.network.message.InnerMessage;
import com.echo.network.message.SocketProtocol;
import com.echo.network.modules.ErrorCode;
import com.echo.network.modules.ErrorCodeModule;
import com.echo.network.protocol.ProtocolContext;
import com.echo.network.protocol.ProtocolMethod;
import com.echo.network.utils.SerializeUtils;

import java.util.concurrent.CompletableFuture;

/**
 * rpc调用Future
 *
 * @author li-yuanwen
 * @date 2021/12/11
 */
public class RpcInvocation extends Invocation {

    /**
     * rpc结果回调future
     **/
    private final CompletableFuture<Object> future;
    /**
     * 回调结果类型
     **/
    private final ProtocolContext protocolContext;

    public RpcInvocation(long sn, Long parentSn, long identity, boolean sync
            , ProtocolContext protocolContext, CompletableFuture<Object> future) {
        super(sn, parentSn, identity, sync);
        this.future = future;
        this.protocolContext = protocolContext;
    }

    @Override
    public void complete(InnerMessage message) {
        SocketProtocol protocol = message.getProtocol();
        // 如果是返回错误码
        if (protocol.getModule() == ErrorCodeModule.MODULE) {
            // 解析错误码消息
            ErrorCode errorCode = SerializeUtils.deserialize(message.getSerializeType(), message.getBody(), ErrorCode.class);
            future.completeExceptionally(new SocketException(errorCode.getCode(), "请求远程服务异常"));
            return;
        }
        ProtocolMethod protocolMethod = protocolContext.getProtocol(protocol);
        Object result = null;
        if (ArrayUtil.isNotEmpty(message.getBody())) {
            result = SerializeUtils.deserialize(message.getSerializeType(), message.getBody(), protocolMethod.getReturnClz());
        }
        future.complete(result);
    }

    public CompletableFuture<Object> getFuture() {
        return future;
    }
}
