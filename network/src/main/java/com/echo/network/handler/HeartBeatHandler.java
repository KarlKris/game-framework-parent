package com.echo.network.handler;

import com.echo.network.message.IMessage;
import com.echo.network.message.InnerMessage;
import com.echo.network.message.OuterMessage;
import com.echo.network.message.ProtocolConstant;
import com.echo.network.protocol.ChannelAttributeKeys;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author li-yuanwen
 * 心跳处理
 */
@Slf4j
@ChannelHandler.Sharable
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    /**
     * 是否开启心跳
     **/
    private final boolean enable;

    public HeartBeatHandler(boolean enable) {
        this.enable = enable;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof IMessage) {
            IMessage message = (IMessage) msg;
            // 修改通信协议
            ctx.channel().attr(ChannelAttributeKeys.LAST_PROTOCOL_HEADER_IDENTITY).set(message.getProtocolHeaderIdentity());

            if (message.isHeartBeatRequest()) {
                if (message.isInnerMessage()) {
                    // 发生心跳响应包
                    ctx.channel().writeAndFlush(InnerMessage.HEART_BEAT_RES);
                    return;
                }

                if (message.isOuterMessage()) {
                    // 发生心跳响应包
                    ctx.channel().writeAndFlush(OuterMessage.HEART_BEAT_RES);
                    return;
                }

                if (log.isWarnEnabled()) {
                    log.warn("收到协议头[{}],暂不支持进行心跳响应,忽略", message.getProtocolHeaderIdentity());
                }

                return;
            }
            if (message.isHeartBeatResponse()) {
                if (log.isDebugEnabled()) {
                    log.debug("收到心跳响应包,忽略");
                }
                return;
            }
        }
        super.channelRead(ctx, msg);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!(evt instanceof IdleStateEvent)) {
            super.userEventTriggered(ctx, evt);
            return;
        }
        // 开启心跳,则向对方发送心跳检测包
        if (enable) {
            Short protocolHeaderIdentity = ctx.channel().attr(ChannelAttributeKeys.LAST_PROTOCOL_HEADER_IDENTITY).get();
            if (protocolHeaderIdentity == null) {
                // 未正常通信过,忽略
                return;
            }
            if (protocolHeaderIdentity == ProtocolConstant.PROTOCOL_INNER_HEADER_IDENTITY) {
                // 发生心跳检测包
                ctx.channel().writeAndFlush(InnerMessage.HEART_BEAT_RES);
                return;
            }

            if (protocolHeaderIdentity == ProtocolConstant.PROTOCOL_OUTER_HEADER_IDENTITY) {
                // 发生心跳检测包
                ctx.channel().writeAndFlush(OuterMessage.HEART_BEAT_RES);
                return;
            }

            if (log.isWarnEnabled()) {
                log.warn("协议头[{}],暂不支持进行心跳检测,断开连接", protocolHeaderIdentity);
            }
            // 关闭连接
            ctx.close();
        } else {
            // 关闭对方连接
            ctx.close();
        }

    }
}
