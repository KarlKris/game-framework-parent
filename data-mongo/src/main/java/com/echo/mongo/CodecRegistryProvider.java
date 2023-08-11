package com.echo.mongo;

import cn.hutool.core.lang.Assert;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.Optional;

/**
 * Provider interface to obtain {@link CodecRegistry} from the underlying MongoDB Java driver.
 */
public interface CodecRegistryProvider {

    /**
     * Get the underlying {@link CodecRegistry} used by the MongoDB Java driver.
     *
     * @return never {@literal null}.
     * @throws IllegalStateException if {@link CodecRegistry} cannot be obtained.
     */
    CodecRegistry getCodecRegistry();

    /**
     * Checks if a {@link Codec} is registered for a given type.
     *
     * @param type must not be {@literal null}.
     * @return true if {@link #getCodecRegistry()} holds a {@link Codec} for given type.
     * @throws IllegalStateException if {@link CodecRegistry} cannot be obtained.
     */
    default boolean hasCodecFor(Class<?> type) {
        return getCodecFor(type).isPresent();
    }

    /**
     * Get the {@link Codec} registered for the given {@literal type} or an {@link Optional#empty() empty Optional}
     * instead.
     *
     * @param type must not be {@literal null}.
     * @param <T>
     * @return never {@literal null}.
     * @throws IllegalArgumentException if {@literal type} is {@literal null}.
     */
    default <T> Optional<Codec<T>> getCodecFor(Class<T> type) {

        Assert.notNull(type, "Type must not be null");

        try {
            return Optional.of(getCodecRegistry().get(type));
        } catch (CodecConfigurationException e) {
            // ignore
        }
        return Optional.empty();
    }

}
