package com.echo.network.session;

import com.echo.common.util.IpUtils;
import com.echo.network.message.IMessage;
import io.netty.channel.Channel;

/**
 * ISession抽象基类
 *
 * @author li-yuanwen
 * @date 2021/12/8
 */
public abstract class AbstractSession implements ISession {


    /**
     * session唯一标识
     **/
    protected final long sessionId;
    /**
     * 连接Channel
     **/
    protected final Channel channel;

    AbstractSession(long sessionId, Channel channel) {
        this.sessionId = sessionId;
        this.channel = channel;
    }


    @Override
    public void writeAndFlush(IMessage message) {
        channel.writeAndFlush(message);
    }

    @Override
    public String getIp() {
        return IpUtils.getIp(this.channel.remoteAddress());
    }

    @Override
    public long getSessionId() {
        return sessionId;
    }

    @Override
    public void close() {
        this.channel.close();
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
}
