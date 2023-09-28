package com.echo.engine.handler;

import com.echo.engine.business.dispatch.AbstractServerBusinessHandler;
import com.echo.network.handler.HeartBeatHandler;
import com.echo.network.handler.MessageDecoder;
import com.echo.network.handler.MessageEncoder;
import com.echo.network.message.ProtocolConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 协议选择器,用来确定双方交换的协议 例如websocket
 *
 * @author: li-yuanwen
 */
@Slf4j
public class ProtocolSelectorHandler extends ByteToMessageDecoder {

    /**
     * WEBSOCKET 握手数据包头
     **/
    final static short WEBSOCKET_HANDSHAKE_PREFIX = ('G' << 8) + 'E';
    /**
     * 协议头字节数
     **/
    final static short PROTOCOL_BYTES_SIZE = Short.BYTES;

    /**
     * 消息最大长度 10M
     **/
    final static int DEFAULT_MAX_MESSAGE_CONTENT_LENGTH = 1024 * 1024 * 10;

    private final int maxMessageContentLength;
    /**
     * handler线程池
     **/
    private final EventExecutorGroup eventExecutorGroup;
    /**
     * 心跳
     **/
    private final HeartBeatHandler heartBeatHandler;

    public ProtocolSelectorHandler() {
        this(DEFAULT_MAX_MESSAGE_CONTENT_LENGTH, null, null);
    }

    public ProtocolSelectorHandler(EventExecutorGroup eventExecutorGroup) {
        this(DEFAULT_MAX_MESSAGE_CONTENT_LENGTH, eventExecutorGroup, null);
    }

    public ProtocolSelectorHandler(int maxMessageContentLength, EventExecutorGroup eventExecutorGroup, HeartBeatHandler heartBeatHandler) {
        this.maxMessageContentLength = maxMessageContentLength;
        this.eventExecutorGroup = eventExecutorGroup;
        this.heartBeatHandler = heartBeatHandler;

        this.messageEncoder = new MessageEncoder();
        this.webSocketEncoder = new WebSocketEncoder(messageEncoder);
    }

    /**
     * messageEncoder
     **/
    private MessageEncoder messageEncoder;
    /**
     * webSocketEncoder
     **/
    private WebSocketEncoder webSocketEncoder;


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 可读字节数小于协议头字节数,忽略
        if (in.readableBytes() < PROTOCOL_BYTES_SIZE) {
            if (log.isDebugEnabled()) {
                log.debug("可读字节数小于协议头字节数[{}],断开连接", PROTOCOL_BYTES_SIZE);
            }

            // 释放ByteBuf
            ReferenceCountUtil.release(in);
            ctx.close();
            return;
        }

        // 读取协议头
        short protocolPrefix = ProtocolConstant.getProtocolHeaderIdentity(in);
        if (protocolPrefix == WEBSOCKET_HANDSHAKE_PREFIX) {
            // 客户端是websocket连接
            String idleStateHandlerName = IdleStateHandler.class.getSimpleName();

            // HttpServerCodec：将请求和应答消息解码为HTTP消息
            ctx.pipeline().addBefore(eventExecutorGroup, idleStateHandlerName
                    , HttpServerCodec.class.getSimpleName(), new HttpServerCodec());

            // HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
            // netty是基于分段请求的，HttpObjectAggregator的作用是将请求分段再聚合,参数是聚合字节的最大长度
            ctx.pipeline().addBefore(eventExecutorGroup, idleStateHandlerName
                    , HttpObjectAggregator.class.getSimpleName(), new HttpObjectAggregator(1024 * 1024 * 10));

            // 主要用于处理大数据流，
            // 比如一个1G大小的文件如果你直接传输肯定会撑暴jvm内存的,增加之后就不用考虑这个问题了
            ctx.pipeline().addBefore(eventExecutorGroup, idleStateHandlerName
                    , ChunkedWriteHandler.class.getSimpleName(), new ChunkedWriteHandler());

            // 针对websocket帧进行聚合解码
            ctx.pipeline().addBefore(eventExecutorGroup, idleStateHandlerName
                    , WebSocketFrameAggregator.class.getSimpleName(), new WebSocketFrameAggregator(1024 * 1024 * 10));

            // websocket数据压缩
            ctx.pipeline().addBefore(eventExecutorGroup, idleStateHandlerName
                    , WebSocketServerCompressionHandler.class.getSimpleName(), new WebSocketServerCompressionHandler());

            // websocket连接处理
            WebSocketServerProtocolConfig config = WebSocketServerProtocolConfig.newBuilder()
                    .allowExtensions(true)
                    .websocketPath("/")
                    .handleCloseFrames(true)
                    .build();
            ctx.pipeline().addBefore(eventExecutorGroup, idleStateHandlerName
                    , WebSocketServerProtocolHandler.class.getSimpleName(), new WebSocketServerProtocolHandler(config));

            // 编解码器
            WebSocketDecoder webSocketDecoder = new WebSocketDecoder();
            ctx.pipeline().addBefore(eventExecutorGroup, idleStateHandlerName
                    , WebSocketEncoder.class.getSimpleName(), this.webSocketEncoder);
            ctx.pipeline().addBefore(eventExecutorGroup, idleStateHandlerName
                    , WebSocketDecoder.class.getSimpleName(), webSocketDecoder);


            if (heartBeatHandler != null) {
                // 心跳
                ctx.pipeline().addBefore(eventExecutorGroup, AbstractServerBusinessHandler.HANDLER_NAME
                        , HeartBeatHandler.class.getSimpleName(), this.heartBeatHandler);
            }

            // 移除自身,完成协议选择
            ctx.pipeline().remove(this.getClass().getSimpleName());

        } else if (protocolPrefix == ProtocolConstant.PROTOCOL_INNER_HEADER_IDENTITY
                || protocolPrefix == ProtocolConstant.PROTOCOL_OUTER_HEADER_IDENTITY) {
            // 自定义协议
            String idleStateHandlerName = IdleStateHandler.class.getSimpleName();

            // 编解码器
            ctx.pipeline().addBefore(eventExecutorGroup, idleStateHandlerName
                    , MessageEncoder.class.getSimpleName(), this.messageEncoder);
            ctx.pipeline().addBefore(eventExecutorGroup, idleStateHandlerName
                    , MessageDecoder.class.getSimpleName(), messageDecoder(maxMessageContentLength));

            if (heartBeatHandler != null) {
                // 心跳
                ctx.pipeline().addBefore(eventExecutorGroup, AbstractServerBusinessHandler.HANDLER_NAME
                        , HeartBeatHandler.class.getSimpleName(), this.heartBeatHandler);
            }

            // 移除自身,完成协议选择
            ctx.pipeline().remove(this.getClass().getSimpleName());

            return;
        }

        // 不支持的协议,忽略
        if (log.isDebugEnabled()) {
            log.debug("接收到协议头[{}],暂不支持该协议,断开连接", protocolPrefix);
        }

        // 释放ByteBuf
        ReferenceCountUtil.release(in);
        ctx.close();
    }


    private MessageDecoder messageDecoder(int maxMessageLength) {
        return new MessageDecoder(maxMessageLength, 2, 4);
    }

}
