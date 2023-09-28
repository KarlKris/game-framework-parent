package com.echo.network.session;

import io.netty.channel.Channel;

/**
 * 外部连接：Client->Gateway
 *
 * @author li-yuanwen
 * @date 2021/12/8
 */
public class PlayerSession extends AbstractSession {

    /**
     * 连接标识
     **/
    private long identity = -1;

    public PlayerSession(long sessionId, Channel channel) {
        super(sessionId, channel);
    }

    @Override
    public PlayerSession bindIdentity(long identity) {
        this.identity = identity;
        return this;
    }

    /**
     * 判断连接是否已辨别身份
     *
     * @return true 是
     */
    public boolean isIdentity() {
        return identity > 0;
    }

    public long getIdentity() {
        return identity;
    }


}
