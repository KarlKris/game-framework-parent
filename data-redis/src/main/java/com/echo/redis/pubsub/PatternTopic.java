package com.echo.redis.pubsub;

import com.echo.common.util.ObjectUtils;

/**
 * @author: li-yuanwen
 */
public class PatternTopic implements Topic {

    private final String channelPattern;

    /**
     * Constructs a new {@link PatternTopic} instance.
     *
     * @param pattern must not be {@literal null}.
     */
    public PatternTopic(String pattern) {

        if (pattern == null) {
            throw new IllegalArgumentException("Pattern must not be null!");
        }

        this.channelPattern = pattern;
    }

    /**
     * Create a new {@link PatternTopic} for channel subscriptions based on a {@code pattern}.
     *
     * @param pattern the channel pattern, must not be {@literal null} or empty.
     * @return the {@link PatternTopic} for {@code pattern}.
     * @since 2.1
     */
    public static PatternTopic of(String pattern) {
        return new PatternTopic(pattern);
    }

    /**
     * @return channel pattern.
     */
    @Override
    public String getTopic() {
        return channelPattern;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return channelPattern;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PatternTopic that = (PatternTopic) o;

        return ObjectUtils.nullSafeEquals(channelPattern, that.channelPattern);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(channelPattern);
    }
}
