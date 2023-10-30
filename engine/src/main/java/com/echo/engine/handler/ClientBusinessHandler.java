package com.echo.engine.handler;

import cn.hutool.core.convert.ConvertException;
import com.echo.common.concurrency.IdentityRunnableLoopGroup;
import com.echo.common.util.ReflectionUtils;
import com.echo.engine.boostrap.NettyServerBootstrap;
import com.echo.engine.business.LocalMessageContext;
import com.echo.engine.rpc.core.Invocation;
import com.echo.engine.rpc.core.InvocationContext;
import com.echo.engine.rpc.push.PushOperation;
import com.echo.network.exception.SerializeFailException;
import com.echo.network.message.InnerMessage;
import com.echo.network.message.PushResponse;
import com.echo.network.protocol.*;
import com.echo.network.utils.SerializeUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * @author li-yuanwen
 */
@Slf4j
@ChannelHandler.Sharable
public class ClientBusinessHandler extends SimpleChannelInboundHandler<InnerMessage> {


    private final ProtocolContext protocolContext;

    private final InvocationContext invocationContext;

    private final PushOperation pushOperation;

    private final IdentityRunnableLoopGroup executor;


    public ClientBusinessHandler(ProtocolContext protocolContext, PushOperation pushOperation
            , InvocationContext invocationContext
            , IdentityRunnableLoopGroup executor) {
        this.protocolContext = protocolContext;
        this.pushOperation = pushOperation;
        this.invocationContext = invocationContext;
        this.executor = executor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InnerMessage msg) throws Exception {
        // 处理从服务端收到的信息
        if (msg.isRequest()) {
            if (log.isDebugEnabled()) {
                log.debug("客户端收到请求信息,忽略");
            }

            return;
        }

        // 处理收到的推送消息
        if (msg.getProtocol() != null && msg.getProtocol().isPushProtocol()) {
            handlePushMessage(msg);
            return;
        }

        Invocation invocation = invocationContext.removeSocketFuture(msg.getSn());
        if (invocation == null) {
            log.warn("客户端收到过期信息,序号[{}],忽略", msg.getSn());
            return;
        }

        Long parentSn = invocation.getParentSn();
        long identity = invocation.getIdentity();
        if (invocation.isSync()) {
            invocation.complete(msg);
        } else {
            Executor executor = null;
            if (identity > 0) {
                executor = this.executor.getExecutor(identity);
            } else {
                executor = this.executor.next();
            }

            executor.execute(() -> {
                LocalMessageContext.setIdentity(identity);
                LocalMessageContext.setMessageSn(parentSn);
                try {
                    invocation.complete(msg);
                } finally {
                    LocalMessageContext.removeIdentity();
                    LocalMessageContext.removeMessageSn();
                }
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            log.error("客户端发生IOException,与服务端断开连接", cause);
            ctx.close();
        } else {
            log.error("客户端发生未知异常", cause);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            // 开启心跳,则向对方发送心跳检测包
            if (event.state() == IdleState.WRITER_IDLE) {
                // 发生心跳检测包
                ctx.channel().writeAndFlush(InnerMessage.HEART_BEAT_REQ);
                return;
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    private void handlePushMessage(InnerMessage message) {
        ProtocolMethodInvocation requestInvocation = protocolContext.getRequestInvocation(message.getProtocol());
        if (requestInvocation == null) {
            // 无处理,即仅是中介,直接推送至外网
            PushResponse pushResponse = SerializeUtils.deserialize(NettyServerBootstrap.SERIALIZE_TYPE
                    , message.getBody(), PushResponse.class);
            pushOperation.pushOuterMessage(pushResponse, message.getProtocol());
            return;
        }
        executor.next().execute(() -> {
            try {
                // 推送中介逻辑处理
                PushResponse pushResponse = SerializeUtils.deserialize(NettyServerBootstrap.SERIALIZE_TYPE
                        , message.getBody(), PushResponse.class);
                ProtocolMethod protocolMethod = requestInvocation.getProtocolMethod();
                MethodParameter[] params = protocolMethod.getParams();
                Object[] args = new Object[params.length];
                for (int i = 0; i < params.length; i++) {
                    if (params[i] instanceof PushIdsMethodParameter) {
                        args[i] = pushResponse.getTargets();
                        continue;
                    }

                    if (params[i] instanceof InBodyMethodParameter) {
                        args[i] = SerializeUtils.deserialize(NettyServerBootstrap.SERIALIZE_TYPE
                                , pushResponse.getContent(), params[i].getParameterClass());
                    }
                }

                ReflectionUtils.invokeMethod(protocolMethod.getMethod(), requestInvocation.getTarget(), args);
            } catch (SerializeFailException e) {
                log.error("发生序列化/反序列化异常", e);
            } catch (ConvertException e) {
                log.error("发生类型转换异常", e);
            } catch (Exception e) {
                log.error("发生未知异常", e);
            }
        });
    }
}
