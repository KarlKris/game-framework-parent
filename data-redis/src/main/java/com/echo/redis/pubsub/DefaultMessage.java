package com.echo.redis.pubsub;

/**
 * @author: li-yuanwen
 */
public class DefaultMessage {

    private static final byte[] EMPTY = new byte[0];
    private final byte[] channel;
    private final byte[] body;
    private String toString;

    public DefaultMessage(byte[] channel, byte[] body) {
        if (channel == null) {
            throw new IllegalArgumentException("Channel must not be null!");
        }
        if (body == null) {
            throw new IllegalArgumentException("Body must not be null!");
        }

        this.body = body;
        this.channel = channel;
    }

    public byte[] getChannel() {
        return channel.length == 0 ? EMPTY : channel.clone();
    }

    public byte[] getBody() {
        return body.length == 0 ? EMPTY : body.clone();
    }

    @Override
    public String toString() {

        if (toString == null) {
            toString = new String(body);
        }
        return toString;
    }

}
