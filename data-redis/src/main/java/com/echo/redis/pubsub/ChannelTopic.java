package com.echo.redis.pubsub;

import com.echo.common.util.ObjectUtils;

/**
 * @author: li-yuanwen
 */
public class ChannelTopic implements Topic {

    private final String channelName;

    /**
     * Constructs a new {@link ChannelTopic} instance.
     *
     * @param name must not be {@literal null}.
     */
    public ChannelTopic(String name) {

        if (name == null) {
            throw new IllegalArgumentException("Topic name must not be null!");
        }

        this.channelName = name;
    }

    /**
     * Create a new {@link ChannelTopic} for channel subscriptions.
     *
     * @param name the channel name, must not be {@literal null} or empty.
     * @return the {@link ChannelTopic} for {@code channelName}.
     * @since 2.1
     */
    public static ChannelTopic of(String name) {
        return new ChannelTopic(name);
    }

    /**
     * @return topic name.
     */
    @Override
    public String getTopic() {
        return channelName;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return channelName;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ChannelTopic that = (ChannelTopic) o;

        return ObjectUtils.nullSafeEquals(channelName, that.channelName);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(channelName);
    }
}
